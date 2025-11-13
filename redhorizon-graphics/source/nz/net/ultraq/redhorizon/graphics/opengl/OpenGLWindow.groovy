/*
 * Copyright 2023, Emanuel Rabina (http://www.ultraq.net.nz/)
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
import nz.net.ultraq.redhorizon.graphics.Window
import nz.net.ultraq.redhorizon.graphics.imgui.DebugOverlay
import nz.net.ultraq.redhorizon.graphics.imgui.GameWindow
import nz.net.ultraq.redhorizon.graphics.imgui.ImGuiContext
import nz.net.ultraq.redhorizon.graphics.imgui.NodeList
import nz.net.ultraq.redhorizon.input.CursorPositionEvent
import nz.net.ultraq.redhorizon.input.KeyEvent
import nz.net.ultraq.redhorizon.input.MouseButtonEvent

import org.joml.primitives.Rectanglei
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GLDebugMessageCallback
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.*
import static org.lwjgl.glfw.GLFWErrorCallback.getDescription
import static org.lwjgl.opengl.GL11C.*
import static org.lwjgl.opengl.GL30C.*
import static org.lwjgl.opengl.KHRDebug.*
import static org.lwjgl.system.MemoryUtil.NULL

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

/**
 * A window using OpenGL as the API.
 *
 * @author Emanuel Rabina
 */
class OpenGLWindow implements Window<OpenGLWindow> {

	private static final Logger logger = LoggerFactory.getLogger(OpenGLWindow)

	private final long window
	int width
	int height
	private int framebufferWidth
	private int framebufferHeight
	final float renderScale
	final Rectanglei viewport
	private boolean centered
	private boolean fullScreen
	private int interval
	private long lastClickTime
	private final Rectanglei lastWindowPositionAndSize = new Rectanglei()
	private final Framebuffer framebuffer
	private final ScreenShader screenShader

	private final ImGuiContext imGuiContext
	private final GameWindow gameWindow
	private DebugOverlay debugOverlay
	private NodeList nodeList
	private boolean showImGuiWindows

	/**
	 * Create and configure a new window with OpenGL.
	 */
	OpenGLWindow(int width, int height, String title) {

		this.width = width
		this.height = height

		glfwSetErrorCallback() { int error, long description ->
			logger.error(getDescription(description))
		}

		if (!glfwInit()) {
			throw new Exception('Unable to initialize GLFW')
		}

		glfwWindowHint(GLFW_DOUBLEBUFFER, GLFW_TRUE)
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
		glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE)
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4)
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1)
		glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE)

		window = glfwCreateWindow(width, height, title, NULL, NULL)
		if (!window) {
			throw new Exception('Failed to create a window')
		}

		(framebufferWidth, framebufferHeight) = getAndTrackFramebufferSize { newWidth, newHeight ->
			// Width/height will be 0 if the window is minimized
			if (newWidth && newHeight) {
				this.width = newWidth * renderScale as int
				this.height = newHeight * renderScale as int
				framebufferWidth = newWidth
				framebufferHeight = newHeight

				var scale = Math.min(newWidth / viewport.lengthX(), newHeight / viewport.lengthY())
				var viewportWidth = (int)(viewport.lengthX() * scale)
				var viewportHeight = (int)(viewport.lengthY() * scale)
				var viewportX = (newWidth - viewportWidth) / 2 as int
				var viewportY = (newHeight - viewportHeight) / 2 as int
				viewport.setMin(viewportX, viewportY).setLengths(viewportWidth, viewportHeight)
				logger.debug('Viewport updated: {}, {}, {}, {}', viewport.minX, viewport.minY, viewport.lengthX(), viewport.lengthY())

				trigger(new FramebufferSizeEvent(newWidth, newHeight))
			}
		}

		renderScale = framebufferWidth / width
		viewport = new Rectanglei(0, 0, framebufferWidth, framebufferHeight)

		makeCurrent()

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

		glEnable(GL_BLEND)
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

		// Input callbacks
		glfwSetKeyCallback(window) { long window, int key, int scancode, int action, int mods ->
			trigger(new KeyEvent(key, scancode, action, mods))
		}
		glfwSetMouseButtonCallback(window) { long window, int button, int action, int mods ->
			trigger(new MouseButtonEvent(button, action, mods))
		}
		glfwSetCursorPosCallback(window) { long window, double xpos, double ypos ->
			trigger(new CursorPositionEvent(xpos * renderScale, ypos * renderScale))
		}

		// Window features

		// Implementation of double-click being used to toggle between windowed and
		// full screen modes.  This isn't natively supported in GLFW given platform
		// differences in double-click behaviour, so we have to roll it ourselves.
		// This is for Windows only, as on macOS fullscreen apps should get their
		// own space which GLFW will not do.
		if (System.isWindows()) {
			on(MouseButtonEvent) { event ->
				if (event.button() == GLFW_MOUSE_BUTTON_1 && event.action() == GLFW_RELEASE) {
					var clickTime = System.currentTimeMillis()
					if (clickTime - lastClickTime < 300) {
						toggleFullScreen()
					}
					lastClickTime = clickTime
				}
			}
		}

		// This framebuffer will be used as the render target for the window, so
		// that we can either send it to ImGui if enabled, or straight to the screen
		// if not.
		framebuffer = new OpenGLFramebuffer(width, height)
		screenShader = new ScreenShader()

		// Create an ImGui context - might as well bake it into the window as we're
		// gonna be using it a lot
		imGuiContext = new ImGuiContext(window, getContentScale() / renderScale as float)
		gameWindow = new GameWindow()
	}

	@Override
	OpenGLWindow addDebugOverlay(DebugOverlay debugOverlay) {

		this.debugOverlay = debugOverlay.withInternals(imGuiContext, this)
		return this
	}

	@Override
	OpenGLWindow addNodeList(NodeList nodeList) {

		this.nodeList = nodeList
		showImGuiWindows = true
		return this
	}

	@Override
	OpenGLWindow centerToScreen() {

		centered = true
		var widthPointer = new int[1]
		var heightPointer = new int[1]
		glfwGetWindowSize(window, widthPointer, heightPointer)
		var width = widthPointer[0]
		var height = heightPointer[0]

		var videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor())
		glfwSetWindowPos(window,
			(videoMode.width() / 2) - (width / 2) as int,
			(videoMode.height() / 2) - (height / 2) as int)
		return this
	}

	@Override
	void clear() {

		glClear(GL_COLOR_BUFFER_BIT)
	}

	@Override
	void close() {

		imGuiContext.close()
		glfwDestroyWindow(window)
		glfwTerminate()
	}

	/**
	 * Return the current framebuffer size and add a listener to the window for
	 * any changes, which will be passed to the {@code closure}.
	 */
	private Tuple2<Integer, Integer> getAndTrackFramebufferSize(
		@ClosureParams(value = SimpleType, options = ['int', 'int']) Closure closure) {

		var widthPointer = new int[1]
		var heightPointer = new int[1]
		glfwGetFramebufferSize(window, widthPointer, heightPointer)
		var width = widthPointer[0]
		var height = heightPointer[0]
		logger.debug('Framebuffer size is {}x{}', width, height)

		glfwSetFramebufferSizeCallback(window) { window, newWidth, newHeight ->
			logger.debug('Framebuffer size changed to {}x{}', newWidth, newHeight)
			closure(newWidth, newHeight)
		}

		return new Tuple2<>(width, height)
	}

	/**
	 * Return the current content scale.
	 */
	private float getContentScale() {

		var contentScalePointer = new float[1]
		glfwGetWindowContentScale(window, contentScalePointer, new float[1])
		var contentScale = contentScalePointer[0]
		logger.debug('Content scale is {}', contentScale)
		return contentScale
	}

	@Override
	void makeCurrent() {

		glfwMakeContextCurrent(window)
	}

	@Override
	void pollEvents() {

		glfwPollEvents()
	}

	@Override
	void releaseCurrent() {

		glfwMakeContextCurrent(NULL)
	}

	@Override
	OpenGLWindow scaleToFit() {

		var videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor())
		// Subtract some value from the monitor size to account for any window and OS chrome
		var scale = Math.floor(Math.min((videoMode.width() * 0.90) / width, (videoMode.height() * 0.90) / height)) as int
		width *= scale
		height *= scale
		glfwSetWindowSize(window, width, height)
		return centered ? centerToScreen() : this
	}

	/**
	 * Set the vsync state on the window.
	 */
	private void setVSync(int interval) {

		this.interval = interval
		glfwSwapInterval(interval)
		logger.debug(
			'VSync x{} ({})',
			interval,
			switch (interval) {
				case 0 -> 'disabled'
				case 1 -> 'enabled'
				case 2 -> '1/2 refresh'
				case 3 -> '1/3 refresh'
				case 4 -> '1/4 refresh'
			}
		)
	}

	@Override
	boolean shouldClose() {

		return glfwWindowShouldClose(window)
	}

	@Override
	void shouldClose(boolean shouldClose) {

		glfwSetWindowShouldClose(window, shouldClose)
	}

	@Override
	OpenGLWindow show() {

		glfwShowWindow(window)
		return this
	}

	@Override
	void swapBuffers() {

		glfwSwapBuffers(window)
	}

	@Override
	void toggleFullScreen() {

		// Switch to window mode
		if (fullScreen) {
			logger.debug('Switching to windowed mode')
			glfwSetWindowMonitor(window, NULL,
				lastWindowPositionAndSize.minX, lastWindowPositionAndSize.minY,
				lastWindowPositionAndSize.lengthX(), lastWindowPositionAndSize.lengthY(),
				-1)
		}

		// Switch to full screen mode
		else {
			logger.debug('Switching to full screen mode')

			var xPointer = new int[1]
			var yPointer = new int[1]
			var widthPointer = new int[1]
			var heightPointer = new int[1]
			glfwGetWindowPos(window, xPointer, yPointer)
			var x = xPointer[0]
			var y = yPointer[0]
			glfwGetWindowSize(window, widthPointer, heightPointer)
			var width = widthPointer[0]
			var height = heightPointer[0]
			logger.debug('Window position and size before maximizing is {}x{}, {}x{}', x, y, width, height)
			lastWindowPositionAndSize.setMin(x, y).setLengths(width, height)

			var primaryMonitor = glfwGetPrimaryMonitor()
			var videoMode = glfwGetVideoMode(primaryMonitor)
			glfwSetWindowMonitor(window, primaryMonitor, 0, 0, videoMode.width(), videoMode.height(), videoMode.refreshRate())
		}

		fullScreen = !fullScreen
	}

	@Override
	void toggleImGuiWindows() {

		showImGuiWindows = !showImGuiWindows
	}

	@Override
	void toggleVSync() {

		setVSync(Math.wrap(interval + 1, 0, System.isWindows() ? 5 : 2))
	}

	@Override
	void useWindow(Closure closure) {

		// Render everything out to the internal framebuffer
		framebuffer.useFramebuffer(closure)

		// Then render everything to the screen, either via ImGui or the underlying window
		glBindFramebuffer(GL_FRAMEBUFFER, 0)
		glDisable(GL_DEPTH_TEST)
		clear()
		glViewport(viewport.minX, viewport.minY, viewport.lengthX(), viewport.lengthY())

		imGuiContext.withFrame(showImGuiWindows) { dockspaceId ->
			if (dockspaceId) {
				gameWindow.render(dockspaceId, framebuffer)
				nodeList.render()
			}
			else {
				screenShader.useShader { shaderContext ->
					framebuffer.draw(shaderContext)
				}
			}
			debugOverlay?.render()
		}
		swapBuffers()
		pollEvents()
	}

	@Override
	OpenGLWindow withBackgroundColour(Colour colour) {

		glClearColor(colour.r, colour.g, colour.b, colour.a)
		return this
	}

	@Override
	OpenGLWindow withMaximized() {

		glfwMaximizeWindow(window)
		return this
	}

	@Override
	OpenGLWindow withVSync(boolean vsync) {

		setVSync(vsync ? 1 : 0)
		return this
	}
}
