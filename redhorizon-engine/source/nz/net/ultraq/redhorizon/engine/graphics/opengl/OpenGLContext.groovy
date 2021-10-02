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

package nz.net.ultraq.redhorizon.engine.graphics.opengl

import nz.net.ultraq.redhorizon.engine.Context
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.input.CursorPositionEvent
import nz.net.ultraq.redhorizon.engine.input.KeyEvent
import nz.net.ultraq.redhorizon.engine.input.MouseButtonEvent
import nz.net.ultraq.redhorizon.engine.input.ScrollEvent
import nz.net.ultraq.redhorizon.events.EventTarget
import nz.net.ultraq.redhorizon.geometry.Dimension

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
class OpenGLContext extends Context implements EventTarget {

	private static final Logger logger = LoggerFactory.getLogger(OpenGLContext)

	// The width of most full-screen graphics in C&C
	private static final int BASE_WIDTH = 320

	// The aspect ratio of a 320x200 image on VGA screens with non-square pixels
	private static final float ASPECT_RATIO_VGA = 4 / 3

	// The aspect ratio of a 320x200 image on modern displays
	private static final float ASPECT_RATIO_MODERN = 16 / 10

	private final GraphicsConfiguration config
	final long window
	final Dimension windowSize
	final Dimension framebufferSize
	final Dimension renderSize

	/**
	 * Constructor, create a new OpenGL window and context using GLFW.
	 * 
	 * @param config
	 */
	OpenGLContext(GraphicsConfiguration config) {

		this.config = config

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

		def monitor = glfwGetPrimaryMonitor()
		def videoMode = glfwGetVideoMode(monitor)
		windowSize = config.fullScreen ?
			new Dimension(videoMode.width(), videoMode.height()) :
			calculateWindowSize(config.fixAspectRatio ? ASPECT_RATIO_VGA : ASPECT_RATIO_MODERN)
		renderSize = new Dimension(640, config.fixAspectRatio ? 480 : 400)

		glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE)
		glfwWindowHint(GLFW_REFRESH_RATE, videoMode.refreshRate())

		glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE)
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4)
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1)
		if (config.debug) {
			glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE)
		}

		logger.debug('Creating a window of size {}', windowSize)
		window = glfwCreateWindow(windowSize.width, windowSize.height, 'Red Horizon', config.fullScreen ? monitor : NULL, NULL)
		if (window == NULL) {
			throw new Exception('Failed to create the GLFW window')
		}

		def widthPointer = new int[1]
		def heightPointer = new int[1]
		glfwGetFramebufferSize(window, widthPointer, heightPointer)
		framebufferSize = new Dimension(widthPointer[0], heightPointer[0])

//		glfwSetFramebufferSizeCallback(window) { long window, int width, int height ->
//			framebufferSize = new Dimension(width, height)
//			trigger(new FramebufferSizeEvent(width, height))
//		}

		// Input callbacks
		glfwSetKeyCallback(window) { long window, int key, int scancode, int action, int mods ->
			trigger(new KeyEvent(key, scancode, action, mods))
		}
		glfwSetScrollCallback(window) { long window, double xoffset, double yoffset ->
			trigger(new ScrollEvent(xoffset, yoffset))
		}
		glfwSetMouseButtonCallback(window) { long window, int button, int action, int mods ->
			trigger(new MouseButtonEvent(button, action, mods))
		}
		glfwSetCursorPosCallback(window) { window, double xpos, double ypos ->
			trigger(new CursorPositionEvent(xpos, ypos))
		}

		withCurrent { ->
			glfwSwapInterval(1)
		}
	}

	/**
	 * Calculate the dimensions for a window that will fit any monitor in a user's
	 * setup, while respecting the target aspect ratio.
	 * 
	 * @param aspectRatio
	 * @return
	 */
	private static Dimension calculateWindowSize(float aspectRatio) {

		// Try get the smallest dimensions of each monitor so that a window cannot
		// be created that exceeds any monitor
		def monitors = glfwGetMonitors()
		def minWidth = 0
		def minHeight = 0
		for (def m = 0; m < monitors.limit(); m++) {
			def monitorVideoMode = glfwGetVideoMode(monitors.get(m))
			minWidth = minWidth ? Math.min(minWidth, monitorVideoMode.width()) : monitorVideoMode.width()
			minHeight = minHeight ? Math.min(minHeight, monitorVideoMode.height()) : monitorVideoMode.height()
		}

		def multiplier = 1
		def widthGap = minWidth / 3
		def heightGap = minHeight / 3
		while (true) {
			def testWidth = BASE_WIDTH * multiplier
			def testHeight = Math.ceil(testWidth / aspectRatio) as int
			if (minWidth - testWidth <= widthGap || minHeight - testHeight <= heightGap) {
				return new Dimension(testWidth, testHeight)
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
