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

import nz.net.ultraq.redhorizon.engine.geometry.Dimension
import nz.net.ultraq.redhorizon.engine.graphics.FramebufferSizeEvent
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsContext
import nz.net.ultraq.redhorizon.engine.graphics.WindowMaximizedEvent
import nz.net.ultraq.redhorizon.engine.input.CursorPositionEvent
import nz.net.ultraq.redhorizon.engine.input.KeyEvent
import nz.net.ultraq.redhorizon.engine.input.MouseButtonEvent
import nz.net.ultraq.redhorizon.engine.input.ScrollEvent
import nz.net.ultraq.redhorizon.events.EventTarget

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
@SuppressWarnings('GrFinalVariableAccess')
class OpenGLContext extends GraphicsContext implements EventTarget {

	private static final Logger logger = LoggerFactory.getLogger(OpenGLContext)

	final long window
	Dimension framebufferSize
	float monitorScale
	Dimension windowSize
	Dimension renderResolution
	Dimension targetResolution

	private final GLFWVidMode videoMode
	private final Dimension windowedSize
	private boolean isFullScreen

	/**
	 * Constructor, create a new OpenGL window and context using GLFW.
	 * 
	 * @param windowTitle
	 * @param config
	 */
	OpenGLContext(String windowTitle, GraphicsConfiguration config) {

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

		// Get the monitor to render to and any information about it
		def monitor = glfwGetPrimaryMonitor()

		if (System.isWindows()) {
			def monitorScaleX = new float[1]
			glfwGetMonitorContentScale(monitor, monitorScaleX, new float[1])
			monitorScale = monitorScaleX[0] // To work with Windows desktop scaling
		}
		else {
			monitorScale = 1f
		}

		videoMode = glfwGetVideoMode(monitor)
		windowedSize = getLargestWindowSize().calculateFit(config.targetAspectRatio) * 0.8f

		// GLFW can't do a proper macOS full screen, so only respect this setting on
		// Windows.  macOS windows can go full screen using the green full screen
		// button.
		isFullScreen = System.isWindows() && config.fullScreen

		windowSize = isFullScreen ? new Dimension(videoMode.width(), videoMode.height()) : windowedSize
		renderResolution = config.renderResolution
		logger.debug('Using a render resolution of {}x{}', renderResolution.width, renderResolution.height)

		// Set OpenGL config for the window we want
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
		glfwWindowHint(GLFW_REFRESH_RATE, videoMode.refreshRate())

		glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE)
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4)
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1)
		if (config.debug) {
			glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE)
		}

		// Create a centered or full screen (borderless) window
		logger.debug('Creating a window of size {}', windowSize)
		window = glfwCreateWindow(windowSize.width, windowSize.height, windowTitle ?: 'Red Horizon', NULL, NULL)
		if (window == NULL) {
			throw new Exception('Failed to create the GLFW window')
		}
		glfwSetWindowPos(window,
			(videoMode.width() / 2) - (windowSize.width / 2) as int,
			(videoMode.height() / 2) - (windowSize.height / 2) as int)
		if (config.maximized) {
			glfwMaximizeWindow(window)
		}
		glfwShowWindow(window)

		// Get the initial framebuffer size
		def widthPointer = new int[1]
		def heightPointer = new int[1]
		glfwGetFramebufferSize(window, widthPointer, heightPointer)
		framebufferSize = new Dimension(widthPointer[0], heightPointer[0])
		targetResolution = framebufferSize.calculateFit(config.targetAspectRatio)
		logger.debug('Using a target resolution of {}x{}', targetResolution.width, targetResolution.height)

		// Track framebuffer size changes from window size changes
		glfwSetFramebufferSizeCallback(window) { long window, int width, int height ->
			trackFramebufferSizeChanges(width, height, config.targetAspectRatio)
			trigger(new FramebufferSizeEvent(framebufferSize, windowSize, targetResolution))
		}

		// Track window minimize/maximize
		glfwSetWindowMaximizeCallback(window) { long window, boolean maximized ->
			trigger(new WindowMaximizedEvent(maximized))
		}

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
		glfwSetCursorPosCallback(window) { long window, double xpos, double ypos ->
			trigger(new CursorPositionEvent(xpos, ypos))
		}

		withCurrent { ->
			glfwSwapInterval(config.vsync ? 1 : 0)
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

	/**
	 * Return dimensions of the largest window that can be created from the user's
	 * monitor setup.  This is so that we don't create a window that exceeds any
	 * monitor
	 * 
	 * @param targetAspectRatio
	 * @return
	 */
	private static Dimension getLargestWindowSize() {

		def monitors = glfwGetMonitors()
		def minWidth = 0
		def minHeight = 0
		for (def m = 0; m < monitors.limit(); m++) {
			def monitorVideoMode = glfwGetVideoMode(monitors.get(m))
			minWidth = minWidth ? Math.min(minWidth, monitorVideoMode.width()) : monitorVideoMode.width()
			minHeight = minHeight ? Math.min(minHeight, monitorVideoMode.height()) : monitorVideoMode.height()
		}
		return new Dimension(minWidth, minHeight)
	}

	@Override
	void makeCurrent() {

		glfwMakeContextCurrent(window)
	}

	/**
	 * Communicate with the window so we're not locking up.
	 */
	@SuppressWarnings('GrMethodMayBeStatic')
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

	@Override
	void toggleFullScreen() {

		def primaryMonitor = glfwGetPrimaryMonitor()
		def videoMode = glfwGetVideoMode(primaryMonitor)

		// Switch to window mode
		if (isFullScreen) {
			logger.debug('Switching to windowed mode')
			glfwSetWindowMonitor(window, NULL,
				(videoMode.width() / 2) - (windowedSize.width / 2) as int,
				(videoMode.height() / 2) - (windowedSize.height / 2) as int,
				windowedSize.width,
				windowedSize.height,
				videoMode.refreshRate())
		}

		// Switch to full screen mode
		else {
			logger.debug('Switching to full screen mode')
			glfwSetWindowMonitor(window, primaryMonitor, 0, 0, videoMode.width(), videoMode.height(), videoMode.refreshRate())
		}

		isFullScreen = !isFullScreen
	}

	/**
	 * Track framebuffer size changes, updating all of the other things that are
	 * based off it.
	 * 
	 * @param width
	 * @param height
	 * @param targetAspectRatio
	 */
	private void trackFramebufferSizeChanges(int width, int height, float targetAspectRatio) {

		logger.debug('Framebuffer changed to {}x{}', width, height)
		framebufferSize = new Dimension(width, height)

		def windowWidthPointer = new int[1]
		def windowHeightPointer = new int[1]
		glfwGetWindowSize(window, windowWidthPointer, windowHeightPointer)
		windowSize = new Dimension(windowWidthPointer[0], windowHeightPointer[0])
		targetResolution = framebufferSize.calculateFit(targetAspectRatio)
		logger.debug('Target resolution changed to {}', targetResolution)
	}

	@Override
	boolean windowShouldClose() {

		return glfwWindowShouldClose(window)
	}

	@Override
	void windowShouldClose(boolean close) {

		glfwSetWindowShouldClose(window, close)
	}
}
