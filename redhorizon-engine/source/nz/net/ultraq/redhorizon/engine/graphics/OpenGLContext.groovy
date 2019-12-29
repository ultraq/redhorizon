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
import nz.net.ultraq.redhorizon.media.Dimension

import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWVidMode
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

	// The width of most full-screen graphics in C&C
	private static final int BASE_WIDTH = 320

	/**
	 * The aspect ratio of a 320x200 image on VGA screens with non-square pixels
	 */
	static final float ASPECT_RATIO_VGA = 4 / 3

	/**
	 * The aspect ratio of a 320x200 image on modern displays
	 */
	static final float ASPECT_RATIO_MODERN = 16 / 10

	final Dimension windowSize

	private final long window

	/**
	 * Constructor, create a new OpenGL window and context.
	 * <p>
	 * A limitation of using GLFW is that this also creates the underlying window
	 * object, through which input events are received.
	 * 
	 * @param aspectRatio
	 */
	OpenGLContext(float aspectRatio) {

		glfwSetErrorCallback(new GLFWErrorCallback() {
			@Override
			void invoke(int error, long description) {
				def message = getDescription(description)
				logger.error(message)
			}
		})

		if (!glfwInit()) {
			throw new IllegalStateException('Unable to initialize GLFW')
		}

		// Try get the dimensions of the main monitor for putting the window in
		def monitor = glfwGetPrimaryMonitor()
		def videoMode = glfwGetVideoMode(monitor)
		windowSize = calculateWindowSizeForFit(videoMode, aspectRatio)

		glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE)
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 1)
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1)
		glfwWindowHint(GLFW_COCOA_RETINA_FRAMEBUFFER, GLFW_TRUE)

		window = glfwCreateWindow(windowSize.width, windowSize.height, 'Red Horizon', NULL, NULL)
		if (window == NULL) {
			throw new Exception('Failed to create the GLFW window')
		}

		withCurrent { ->
			glfwSwapInterval(1)
		}
	}

	/**
	 * Calculate the dimensions for a rendering window that will fit the given
	 * screen size and respect the target aspect ratio. 
	 * 
	 * @param videoMode
	 * @param aspectRatio
	 * @return
	 */
	private static Dimension calculateWindowSizeForFit(GLFWVidMode videoMode, float aspectRatio) {

		def multiplier = 1
		def widthGap = videoMode.width() / 2
		def heightGap = videoMode.height() / 2
		while (true) {
			def testWidth = BASE_WIDTH * multiplier
			def testHeight = Math.ceil(testWidth / aspectRatio)
			if (videoMode.width() - testWidth < widthGap || videoMode.height() - testHeight < heightGap) {
				return new Dimension(testWidth, testHeight as int)
			}
			multiplier++
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
