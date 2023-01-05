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

package nz.net.ultraq.redhorizon.engine.graphics.opengl

import nz.net.ultraq.redhorizon.engine.geometry.Dimension
import nz.net.ultraq.redhorizon.engine.graphics.FramebufferSizeEvent
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.Window
import nz.net.ultraq.redhorizon.engine.graphics.WindowMaximizedEvent
import nz.net.ultraq.redhorizon.engine.input.CursorPositionEvent
import nz.net.ultraq.redhorizon.engine.input.KeyEvent
import nz.net.ultraq.redhorizon.engine.input.MouseButtonEvent
import nz.net.ultraq.redhorizon.engine.input.ScrollEvent

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.*
import static org.lwjgl.system.MemoryUtil.NULL

import groovy.transform.PackageScope
import groovy.transform.TupleConstructor

/**
 * An OpenGL implementation of the window device.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class OpenGLWindow extends Window {

	private static final Logger logger = LoggerFactory.getLogger(OpenGLWindow)

	@PackageScope
	final long window

	final float monitorScale
	final Dimension renderResolution
	Dimension size
	Dimension framebufferSize
	Dimension targetResolution

	private final Dimension windowedSize
	private boolean fullScreen

	/**
	 * Constructor, create a new OpenGL window.
	 *
	 * @param size
	 * @param title
	 */
	OpenGLWindow(String title, GraphicsConfiguration config) {

		// Get the monitor to render to and any information about it
		var monitor = glfwGetPrimaryMonitor()

		if (System.isWindows()) {
			def monitorScaleX = new float[1]
			glfwGetMonitorContentScale(monitor, monitorScaleX, new float[1])
			monitorScale = monitorScaleX[0] // To work with Windows desktop scaling
		}
		else {
			monitorScale = 1f
		}

		var videoMode = glfwGetVideoMode(monitor)
		windowedSize = getLargestWindowSize().calculateFit(config.targetAspectRatio) * 0.8f

		// GLFW can't do a proper macOS full screen, so only respect this setting on
		// Windows.  macOS windows can go full screen using the green full screen
		// button.
		fullScreen = System.isWindows() && config.fullScreen

		size = fullScreen ? new Dimension(videoMode.width(), videoMode.height()) : windowedSize
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
		logger.debug('Creating a window of size {}', size)
		window = glfwCreateWindow(size.width, size.height, title ?: 'Red Horizon', NULL, NULL)
		if (window == NULL) {
			throw new Exception('Failed to create the GLFW window')
		}

		glfwSetWindowPos(window,
			(videoMode.width() / 2) - (size.width / 2) as int,
			(videoMode.height() / 2) - (size.height / 2) as int)
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
			logger.debug('Framebuffer changed to {}x{}', width, height)
			framebufferSize = new Dimension(width, height)

			def windowWidthPointer = new int[1]
			def windowHeightPointer = new int[1]
			glfwGetWindowSize(window, windowWidthPointer, windowHeightPointer)
			size = new Dimension(windowWidthPointer[0], windowHeightPointer[0])
			targetResolution = framebufferSize.calculateFit(config.targetAspectRatio)
			logger.debug('Target resolution changed to {}', targetResolution)

			trigger(new FramebufferSizeEvent(framebufferSize, size, targetResolution))
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
	}

	@Override
	void close() {

		glfwDestroyWindow(window)
	}

	@Override
	long getHandle() {

		return window
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
	void pollEvents() {

		glfwPollEvents()
	}

	@Override
	boolean shouldClose() {

		return glfwWindowShouldClose(window)
	}

	@Override
	void shouldClose(boolean close) {

		glfwSetWindowShouldClose(window, close)
	}

	@Override
	void swapBuffers() {

		glfwSwapBuffers(window)
	}

	@Override
	void toggleFullScreen() {

		def primaryMonitor = glfwGetPrimaryMonitor()
		def videoMode = glfwGetVideoMode(primaryMonitor)

		// Switch to window mode
		if (fullScreen) {
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

		fullScreen = !fullScreen
	}
}
