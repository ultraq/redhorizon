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

import nz.net.ultraq.redhorizon.graphics.Window

import org.lwjgl.glfw.GLFWErrorCallback
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.*
import static org.lwjgl.system.MemoryUtil.NULL

/**
 * A window using OpenGL as the API.
 *
 * @author Emanuel Rabina
 */
class OpenGLWindow implements Window {

	private static final Logger logger = LoggerFactory.getLogger(OpenGLWindow)

	private final long window
	private boolean vsync

	/**
	 * Create and configure a new window with OpenGL.
	 */
	OpenGLWindow(int width, int height, String title) {

		glfwSetErrorCallback(new GLFWErrorCallback() {
			@Override
			void invoke(int error, long description) {
				logger.error(getDescription(description))
			}
		})

		if (!glfwInit()) {
			throw new Exception('Unable to initialize GLFW')
		}

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
		glfwSetWindowPos(window, (videoMode.width() / 2) - (width / 2) as int, (videoMode.height() / 2) - (height / 2) as int)

		makeCurrent()
	}

	@Override
	void beginFrame() {

		// Anything to do here?
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
	Window show() {

		glfwShowWindow(window)
		return this
	}

	@Override
	void toggleBorderlessWindowed() {

	}

	@Override
	void toggleVsync() {

		glfwSwapInterval(vsync ? 0 : 1)
		vsync = !vsync
		logger.debug("Vsync ${vsync ? 'enabled' : 'disabled'}")
	}

	@Override
	Window withBorderlessWindowed() {

		return this
	}

	@Override
	Window withMaximized() {

		glfwMaximizeWindow(window)
		return this
	}

	@Override
	Window withVSync() {

		glfwSwapInterval(1)
		vsync = true
		return this
	}
}
