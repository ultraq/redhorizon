/* 
 * Copyright 2019, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine.graphics

import nz.net.ultraq.redhorizon.engine.AbstractContext

import org.lwjgl.glfw.GLFWErrorCallback
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.*
import static org.lwjgl.system.MemoryUtil.NULL

/**
 * The OpenGL context, a concept used by OpenGL to control rendering threads.
 * Using GLFW, this object represents both the window and the OpenGL context.
 * 
 * @author Emanuel Rabina
 */
class OpenGLContext extends AbstractContext {

	private static final Logger logger = LoggerFactory.getLogger(OpenGLContext)

	private final long window
	final int width = 640
	final int height = 480

	/**
	 * Constructor, create a new OpenGL context.  A limitation of using GLFW is
	 * that this also creates the underlying window object, through which input
	 * events are received.
	 */
	OpenGLContext() {

		glfwSetErrorCallback({ int error, long description ->
			def message = getDescription(description)
			logger.error(message)
		} as GLFWErrorCallback)

		if (!glfwInit()) {
			throw new IllegalStateException('Unable to initialize GLFW')
		}

		glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE)
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 1)
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1)
		glfwWindowHint(GLFW_COCOA_RETINA_FRAMEBUFFER, GLFW_TRUE)

		window = glfwCreateWindow(width, height, 'Red Horizon', NULL, NULL)
		if (window == NULL) {
			throw new Exception('Failed to create the GLFW window')
		}

		withCurrent { ->
			glfwSwapInterval(1)
		}
	}

	/**
	 * Destroys the OpenGL context, which includes the window through which it is
	 * rendering.
	 */
	@Override
	void close() {

		glfwDestroyWindow(window)
		glfwTerminate()
	}

	@Override
	void makeCurrent() {

		glfwMakeContextCurrent(window)
	}

	/**
	 * Communicate with the window so we're not locking up.
	 */
	void pollEvents() {

		glfwPollEvents()
	}

	@Override
	void releaseCurrent() {

		glfwMakeContextCurrent(NULL)
	}

	/**
	 * Swap between the front and back buffers, pushing the new frame to the
	 * display.
	 */
	void swapBuffers() {

		glfwSwapBuffers(window)
	}

	/**
	 * Return whether or not the underlying window has signalled to be closed.
	 * 
	 * @return
	 */
	boolean windowShouldClose() {

		return glfwWindowShouldClose(window)
	}

	/**
	 * Manually set whether or not the underlying window should close.
	 * 
	 * @param close
	 */
	void windowShouldClose(boolean close) {

		glfwSetWindowShouldClose(window, close)
	}
}