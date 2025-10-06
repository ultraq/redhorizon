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

import nz.net.ultraq.eventhorizon.EventTarget
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.FramebufferSizeEvent
import nz.net.ultraq.redhorizon.graphics.Window
import nz.net.ultraq.redhorizon.graphics.imgui.FpsCounter
import nz.net.ultraq.redhorizon.graphics.imgui.ImGuiContext
import nz.net.ultraq.redhorizon.graphics.imgui.NodeList
import nz.net.ultraq.redhorizon.input.CursorPositionEvent
import nz.net.ultraq.redhorizon.input.KeyEvent
import nz.net.ultraq.redhorizon.input.MouseButtonEvent
import nz.net.ultraq.redhorizon.scenegraph.Scene

import org.joml.Vector2i
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
class OpenGLWindow implements Window, EventTarget<OpenGLWindow> {

	private static final Logger logger = LoggerFactory.getLogger(OpenGLWindow)

	private final long window
	float contentScale
	private Vector2i framebufferSize
	final Rectanglei viewport
	private boolean fullScreen
	private boolean vsync
	private long lastClickTime
	private final Rectanglei lastWindowPositionAndSize = new Rectanglei()

	private final ImGuiContext imGuiContext
	private FpsCounter fpsCounter
	private NodeList nodeList

	/**
	 * Create and configure a new window with OpenGL.
	 */
	OpenGLWindow(int framebufferWidth, int framebufferHeight, String title) {

		glfwSetErrorCallback() { int error, long description ->
			logger.error(getDescription(description))
		}

		if (!glfwInit()) {
			throw new Exception('Unable to initialize GLFW')
		}

		glfwWindowHint(GLFW_DOUBLEBUFFER, GLFW_TRUE)
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)
		glfwWindowHint(GLFW_SCALE_TO_MONITOR, GLFW_TRUE)
		glfwWindowHint(GLFW_SCALE_FRAMEBUFFER, GLFW_TRUE)
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
		glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE)
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4)
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1)
		glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE)

		window = glfwCreateWindow(framebufferWidth, framebufferHeight, title, NULL, NULL)
		if (window == NULL) {
			throw new Exception('Failed to create a window')
		}

		contentScale = getAndTrackContentScale { newContentScale ->
			contentScale = newContentScale
		}

		framebufferSize = getAndTrackFramebufferSize { width, height ->
			// Width/height will be 0 if the window is minimized
			if (width && height) {
				framebufferSize.set(width, height)

				var scale = Math.min(width / viewport.lengthX(), height / viewport.lengthY())
				var viewportWidth = (int)(viewport.lengthX() * scale)
				var viewportHeight = (int)(viewport.lengthY() * scale)
				var viewportX = (width - viewportWidth) / 2 as int
				var viewportY = (height - viewportHeight) / 2 as int
				viewport.set(viewportX, viewportY, viewportX + viewportWidth, viewportY + viewportHeight)
				logger.debug('Viewport updated: {}, {}, {}, {}', viewport.minX, viewport.minY, viewport.lengthX(), viewport.lengthY())

				trigger(new FramebufferSizeEvent(framebufferWidth, height))
			}
		}

		viewport = new Rectanglei(0, 0, framebufferSize.x, framebufferSize.y)

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

		glEnable(GL_DEPTH_TEST)
		glDepthFunc(GL_LEQUAL)
		glEnable(GL_BLEND)
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
		glEnable(GL_LINE_SMOOTH)

		// Input callbacks
		glfwSetKeyCallback(window) { long window, int key, int scancode, int action, int mods ->
			trigger(new KeyEvent(key, scancode, action, mods))
		}
		glfwSetMouseButtonCallback(window) { long window, int button, int action, int mods ->
			trigger(new MouseButtonEvent(button, action, mods))
		}
		glfwSetCursorPosCallback(window) { long window, double xpos, double ypos ->
			// On macOS, adjust the cursor position by the scaling factor to account
			// for the difference between the framebuffer size (reported w/ retina
			// scaling) and the cursor position (not reported w/ retina scaling)
			if (System.isMacOs()) {
				trigger(new CursorPositionEvent(xpos * contentScale, ypos * contentScale))
			}
			else {
				trigger(new CursorPositionEvent(xpos, ypos))
			}
		}

		// Window features

		// Implementation of double-click being used to toggle between windowed and
		// full screen modes.  This isn't natively supported in GLFW given platform
		// differences in double-click behaviour, so we have to roll it ourselves.
		// This is for Windows only, as on macOS fullscreen apps should get their
		// own space, which GLFW will not do.
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

		// Create an ImGui context - might as well bake it into the window as we're
		// gonna be using it a lot
		imGuiContext = new ImGuiContext(window, System.isMacOs() ? 1f : contentScale)
	}

	@Override
	OpenGLWindow addFpsCounter(float updateRateSeconds = 0f) {

		fpsCounter = new FpsCounter(imGuiContext, updateRateSeconds)
		return this
	}

	@Override
	OpenGLWindow addNodeList(Scene scene) {

		nodeList = new NodeList(scene)
		return this
	}

	@Override
	OpenGLWindow centerToScreen() {

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

		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)
	}

	@Override
	void close() {

		nodeList?.close()
		fpsCounter?.close()
		imGuiContext.close()
		glfwDestroyWindow(window)
		glfwTerminate()
	}

	/**
	 * Return the current content scale and add a listener to the window for any
	 * changes, which will be passed to the {@code closure}.
	 */
	private float getAndTrackContentScale(@ClosureParams(value = SimpleType, options = 'float') Closure closure) {

		var contentScalePointer = new float[1]
		glfwGetWindowContentScale(window, contentScalePointer, new float[1])
		var contentScale = contentScalePointer[0]
		logger.debug('Content scale is {}', contentScale)

		glfwSetWindowContentScaleCallback(window) { window, xscale, yscale ->
			logger.debug('Content scale changed to {}', xscale)
			closure(xscale)
		}

		return contentScale
	}

	/**
	 * Return the current framebuffer size and add a listener to the window for
	 * any changes, which will be passed to the {@code closure}.
	 */
	private Vector2i getAndTrackFramebufferSize(@ClosureParams(value = SimpleType, options = ['int', 'int']) Closure closure) {

		var widthPointer = new int[1]
		var heightPointer = new int[1]
		glfwGetFramebufferSize(window, widthPointer, heightPointer)
		var framebufferSize = new Vector2i(widthPointer[0], heightPointer[0])
		logger.debug('Framebuffer size is {}x{}', framebufferSize.x, framebufferSize.y)

		glfwSetFramebufferSizeCallback(window) { window, width, height ->
			logger.debug('Framebuffer size changed to {}x{}', width, height)
			closure(width, height)
		}

		return framebufferSize
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

	/**
	 * Set the vsync state on the window.
	 */
	private void setVSync(boolean vsync) {

		this.vsync = vsync
		glfwSwapInterval(vsync ? 1 : 0)
		logger.debug("VSync ${vsync ? 'enabled' : 'disabled'}")
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
			lastWindowPositionAndSize.set(x, y, x + width, y + height)

			var primaryMonitor = glfwGetPrimaryMonitor()
			var videoMode = glfwGetVideoMode(primaryMonitor)
			glfwSetWindowMonitor(window, primaryMonitor, 0, 0, videoMode.width(), videoMode.height(), videoMode.refreshRate())
		}

		fullScreen = !fullScreen
	}

	@Override
	void toggleVSync() {

		setVSync(!this.vsync)
	}

	@Override
	void useWindow(Closure closure) {

		glBindFramebuffer(GL_FRAMEBUFFER, 0)
		clear()
		glViewport(viewport.minX, viewport.minY, viewport.lengthX(), viewport.lengthY())
		imGuiContext.withFrame { ->
			closure()
			nodeList?.render()
			fpsCounter?.render()
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

		setVSync(vsync)
		return this
	}
}
