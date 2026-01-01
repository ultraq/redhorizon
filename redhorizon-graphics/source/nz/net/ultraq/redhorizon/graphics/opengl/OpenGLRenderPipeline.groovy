/*
 * Copyright 2025, Emanuel Rabina (http://www.ultraq.net.nz/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nz.net.ultraq.redhorizon.graphics.opengl

import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.Framebuffer
import nz.net.ultraq.redhorizon.graphics.FramebufferSizeEvent
import nz.net.ultraq.redhorizon.graphics.RenderPipeline
import nz.net.ultraq.redhorizon.graphics.imgui.GameWindow
import nz.net.ultraq.redhorizon.graphics.imgui.ImGuiLayer

import org.joml.primitives.Rectanglei
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GLDebugMessageCallback
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.opengl.GL11C.*
import static org.lwjgl.opengl.GL20C.glUseProgram
import static org.lwjgl.opengl.GL30C.*
import static org.lwjgl.opengl.KHRDebug.*

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

/**
 * An OpenGL implementation of each of the discrete steps used in creating a
 * single frame and emitting it to the screen.
 *
 * @author Emanuel Rabina
 */
class OpenGLRenderPipeline implements RenderPipeline, AutoCloseable {

	private static final Logger logger = LoggerFactory.getLogger(OpenGLRenderPipeline)

	private final OpenGLWindow window
	private final ScreenShader screenShader
	private final Rectanglei _viewport

	private final ImGuiLayer imGuiLayer
	private final GameWindow gameWindow
	private boolean dockspaceUsed = false
	private final Rectanglei imGuiViewport = new Rectanglei()

	private Framebuffer sceneResult
	private Framebuffer postProcessingResult

	/**
	 * Constructor, set up a new render pipeline object.
	 */
	OpenGLRenderPipeline(OpenGLWindow window, long windowHandle) {

		this.window = window

		var contentScale = window.contentScale
		var renderScale = window.renderScale

		// Enable debug mode if supported (Windows)
		var capabilities = GL.createCapabilities()
		if (capabilities.GL_KHR_debug) {
			glEnable(GL_DEBUG_OUTPUT)
			glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS)
			glDebugMessageCallback(new GLDebugMessageCallback() {
				@Override
				void invoke(int source, int type, int id, int severity, int length, long message, long userParam) {
					if (severity != GL_DEBUG_SEVERITY_NOTIFICATION) {
						throw new Exception("OpenGL error: ${getMessage(length, message)}")
					}
				}
			}, 0)
		}
		logger.info('OpenGL device: {}, version {}', glGetString(GL_RENDERER), glGetString(GL_VERSION))

		_viewport = new Rectanglei(0, 0, window.width * renderScale as int, window.height * renderScale as int)

		window.on(FramebufferSizeEvent) { event ->
			var newWidth = event.width()
			var newHeight = event.height()
			var scale = Math.min(newWidth / _viewport.lengthX(), newHeight / _viewport.lengthY())
			var viewportWidth = _viewport.lengthX() * scale
			var viewportHeight = _viewport.lengthY() * scale
			var viewportX = (newWidth - viewportWidth) / 2
			var viewportY = (newHeight - viewportHeight) / 2
			_viewport.setMin(viewportX as int, viewportY as int).setLengths(viewportWidth as int, viewportHeight as int)
			logger.debug('Viewport updated: {}, {}, {}, {}', _viewport.minX, _viewport.minY, _viewport.lengthX(), _viewport.lengthY())
		}

		glEnable(GL_BLEND)
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

		screenShader = new ScreenShader()

		// Create an ImGui layer - might as well bake it into the window as we're
		// gonna be using it a lot
		imGuiLayer = new ImGuiLayer(windowHandle, contentScale / renderScale as float)
		gameWindow = new GameWindow()
	}

	@Override
	void close() {

		imGuiLayer.close()
		screenShader.close()
	}

	@Override
	void end() {

		window.swapBuffers()
		window.pollEvents()
	}

	/**
	 * Return the viewport into which the scene is being rendered.  If ImGui
	 * docking is being used, then this will be the area that the primary
	 * framebuffer is occupying in the overall window.  Otherwise, it's just the
	 * window.
	 */
	Rectanglei getViewport() {

		return dockspaceUsed ?
			imGuiViewport
				.setMin(gameWindow.lastImageX as int, gameWindow.lastImageY as int)
				.setLengths(gameWindow.lastImageWidth as int, gameWindow.lastImageHeight as int)
				.scale(window.renderScale as int) :
			_viewport
	}

	@Override
	OpenGLRenderPipeline postProcessing(
		@ClosureParams(value = SimpleType, options = 'nz.net.ultraq.redhorizon.graphics.Framebuffer') Closure<Framebuffer> closure) {

		postProcessingResult = closure(sceneResult)
		return this
	}

	@Override
	OpenGLRenderPipeline scene(Closure<Framebuffer> closure) {

		sceneResult = closure()
		return this
	}

	/**
	 * Update the clear colour for the next invocation of {@code glClear}.
	 */
	void setClearColour(Colour clearColour) {

		glClearColor(clearColour.r, clearColour.g, clearColour.b, clearColour.a)
		gameWindow.setBackgroundColour(clearColour)
	}

	@Override
	OpenGLRenderPipeline ui(
		boolean createDockspace,
		@ClosureParams(value = SimpleType, options = 'nz.net.ultraq.redhorizon.graphics.imgui.ImGuiContext') Closure closure) {

		dockspaceUsed = createDockspace
		useScreen { ->
			imGuiLayer.useImGui(createDockspace) { imGuiContext ->
				if (imGuiContext.dockspaceId) {
					gameWindow.render(imGuiContext.dockspaceId, postProcessingResult ?: sceneResult)
				}
				else {
					screenShader.useShader { shaderContext ->
						(postProcessingResult ?: sceneResult).draw(shaderContext)
					}
				}
				closure(imGuiContext)
			}
		}
		return this
	}

	/**
	 * Use the screen as the render target.
	 */
	private void useScreen(Closure closure) {

		glBindFramebuffer(GL_FRAMEBUFFER, 0)
		glDisable(GL_DEPTH_TEST)

		// Reset shader program when switching framebuffer.  Fixes an issue w/
		// nVidia on Windows where calling glClear() would then cause the following
		// error:
		// "Program/shader state performance warning: Vertex shader in program X
		// is being recompiled based on GL state"
		glUseProgram(0)

		glClear(GL_COLOR_BUFFER_BIT)
		glViewport(_viewport.minX, _viewport.minY, _viewport.lengthX(), _viewport.lengthY())
		closure()
	}
}
