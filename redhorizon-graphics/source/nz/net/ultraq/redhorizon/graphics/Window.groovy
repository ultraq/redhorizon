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

package nz.net.ultraq.redhorizon.graphics

import nz.net.ultraq.redhorizon.graphics.imgui.DebugOverlay
import nz.net.ultraq.redhorizon.graphics.imgui.NodeList
import nz.net.ultraq.redhorizon.input.InputSource

import org.joml.primitives.Rectanglei

/**
 * The video interface through which the graphics are rendered.  One must first
 * be created before any other graphics operations can be performed.
 *
 * <p>When a new window is created, the thread on which it was created is made
 * "current", meaning graphics can only be rendered on that thread.  To render
 * graphics from a different thread, call {@link #makeCurrent}, your commands,
 * then {@link #releaseCurrent}.  Alternatively, use the {@link #withCurrent}
 * method to wrap any closure passed to it with the make/release calls
 * automatically.
 *
 * <p>A window is initially created as hidden so that it can be configured
 * further before it is shown, preventing a window from jumping around the place
 * before it is ready.  When it is ready to be shown, call {@link #show}.
 *
 * @author Emanuel Rabina
 */
interface Window<TWindow extends Window> extends InputSource<TWindow>, AutoCloseable {

	/**
	 * Include a debug overlay with the window.
	 */
	Window addDebugOverlay(DebugOverlay debugOverlay)

	/**
	 * Include a node list with the window.
	 *
	 * <p>Note that by doing this, ImGui's docking mode will be enabled so that
	 * you can place the node list where you like.
	 */
	Window addNodeList(NodeList nodeList)

	/**
	 * Center the window to the screen.
	 *
	 * <p>This may be called after the window has been shown.
	 */
	Window centerToScreen()

	/**
	 * Clear the framebuffer with the current clear colour.  Usually called to
	 * start rendering a new frame.
	 */
	void clear()

	/**
	 * Return the height of the window contents.
	 */
	int getHeight()

	/**
	 * Get the scaling factor between the window size and the framebuffer size.
	 * On most systems this should be 1, but on macOS with retina screens, it's
	 * 2.
	 */
	float getRenderScale()

	/**
	 * Get the viewport used for rendering to the window.
	 */
	Rectanglei getViewport()

	/**
	 * Return the width of the window contents.
	 */
	int getWidth()

	/**
	 * Makes the context current on the executing thread.
	 */
	void makeCurrent()

	/**
	 * Poll for and process all pending events.
	 */
	void pollEvents()

	/**
	 * Releases the context that is current on the executing thread.
	 */
	void releaseCurrent()

	/**
	 * Scale the window width/height by the largest whole number that will still
	 * allow the window to fit within the default monitor.  If not positioned
	 * correctly, this can make the window fall outside of the screen, so you can
	 * follow this up with a call to {@link #centerToScreen} to bring the window
	 * back into view.
	 *
	 * <p>This may be called after the window has been shown.
	 */
	Window scaleToFit()

	/**
	 * Return whether or not the underlying window has signalled to be closed.
	 */
	boolean shouldClose()

	/**
	 * Control the close flag for the window.
	 */
	void shouldClose(boolean shouldClose)

	/**
	 * Show the window.
	 */
	Window show()

	/**
	 * Swap the front/back buffers to push the rendered result to the screen.
	 */
	void swapBuffers()

	/**
	 * Switch between windowed and fullscreen modes.
	 */
	void toggleFullScreen()

	/**
	 * Switch between having ImGui windows rendered or not.
	 */
	void toggleImGuiWindows()

	/**
	 * Switch between vertical sync being anabled/disabled.
	 */
	void toggleVSync()

	/**
	 * Convenience method for using the window as the render target.  The closure
	 * will be surrounded with the necessary {@link #clear}, {@link #swapBuffers},
	 * and {@link #pollEvents} calls.
	 */
	void useWindow(Closure closure)

	/**
	 * Set the background colour of the window.
	 */
	Window withBackgroundColour(Colour colour)

	/**
	 * Surround the given closure with calls to {@link #makeCurrent} and
	 * {@link #releaseCurrent} so that audio commands can be executed in the
	 * current thread.
	 */
	default <T> T withCurrent(Closure<T> closure) {

		try {
			makeCurrent()
			return closure()
		}
		finally {
			releaseCurrent()
		}
	}

	/**
	 * Maximize the window
	 */
	Window withMaximized()

	/**
	 * Set whether vsync is enabled/disabled for this window.
	 */
	Window withVSync(boolean vsync)
}
