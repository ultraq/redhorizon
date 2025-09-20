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
import nz.net.ultraq.redhorizon.graphics.Window
import nz.net.ultraq.redhorizon.input.CursorPositionEvent
import nz.net.ultraq.redhorizon.input.KeyEvent
import nz.net.ultraq.redhorizon.input.MouseButtonEvent

import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GLDebugMessageCallback
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.*
import static org.lwjgl.glfw.GLFWErrorCallback.*
import static org.lwjgl.opengl.GL11C.*
import static org.lwjgl.opengl.KHRDebug.*
import static org.lwjgl.system.MemoryUtil.*

/**
 * A window using OpenGL as the API.
 *
 * @author Emanuel Rabina
 */
class OpenGLWindow implements Window, EventTarget<OpenGLWindow> {

	private static final Logger logger = LoggerFactory.getLogger(OpenGLWindow)

	final int width
	final int height
	private final long window
	private boolean vsync

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
		glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE)
		glfwWindowHint(GLFW_SCALE_TO_MONITOR, GLFW_TRUE)
		glfwWindowHint(GLFW_SCALE_FRAMEBUFFER, GLFW_TRUE)
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
		glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE)
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4)
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1)
		glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE)

		window = glfwCreateWindow(width, height, title, NULL, NULL)
		if (window == NULL) {
			throw new Exception('Failed to create a window')
		}

		var primaryMonitor = glfwGetPrimaryMonitor()
		var videoMode = glfwGetVideoMode(primaryMonitor)
		var contentScalePointer = new float[1]
		glfwGetWindowContentScale(window, contentScalePointer, new float[1])
		var contentScale = contentScalePointer[0]
		glfwSetWindowPos(window,
			(videoMode.width() / 2) - ((width * contentScale) / 2) as int,
			(videoMode.height() / 2) - ((height * contentScale) / 2) as int)

		makeCurrent()

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

		// Input callbacks
		glfwSetKeyCallback(window) { long window, int key, int scancode, int action, int mods ->
			trigger(new KeyEvent(key, scancode, action, mods))
		}
		glfwSetMouseButtonCallback(window) { long window, int button, int action, int mods ->
			trigger(new MouseButtonEvent(button, action, mods))
		}
		glfwSetCursorPosCallback(window) { long window, double xpos, double ypos ->
			trigger(new CursorPositionEvent(xpos, ypos))
		}
	}

	@Override
	void beginFrame() {

		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)
	}

	@Override
	void close() {

		glfwDestroyWindow(window)
		glfwTerminate()
	}

	@Override
	void endFrame() {

		glfwSwapBuffers(window)
		glfwPollEvents()
	}

	@Override
	void makeCurrent() {

		glfwMakeContextCurrent(window)
	}

	@Override
	void releaseCurrent() {

		glfwMakeContextCurrent(NULL)
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
	void toggleBorderlessWindowed() {

		// TODO
	}

	@Override
	void toggleVsync() {

		this.vsync = !vsync
		glfwSwapInterval(vsync ? 1 : 0)
		logger.debug("Vsync ${vsync ? 'enabled' : 'disabled'}")
	}

	@Override
	OpenGLWindow withBackgroundColour(Colour colour) {

		glClearColor(colour.r, colour.g, colour.b, colour.a)
		return this
	}

	@Override
	OpenGLWindow withBorderlessWindowed() {

		// TODO
		return this
	}

	@Override
	OpenGLWindow withMaximized() {

		glfwMaximizeWindow(window)
		return this
	}

	@Override
	OpenGLWindow withVSync(boolean vsync) {

		this.vsync = vsync
		glfwSwapInterval(vsync ? 1 : 0)
		return this
	}
}
