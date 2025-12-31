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
	 * Center the window to the screen.
	 *
	 * <p>This may be called after the window has been shown.
	 */
	Window centerToScreen()

	/**
	 * Return the window's content scaling.  This is the amount a user scales
	 * their desktop by.
	 */
	float getContentScale()

	/**
	 * Return the height of the window.
	 */
	int getHeight()

	/**
	 * Return the window's "render scaling".  This is the factor between the
	 * requested window size and the size of the framebuffer that was created from
	 * it (eg: if requesting a window of 800 pixels across, but we get a
	 * framebuffer of 1600 pixels across, then the render scale is 2) and is
	 * usually set by the OS to account for high DPI displays (eg: macOS on their
	 * retina displays).
	 */
	float getRenderScale()

	/**
	 * Get the viewport used for rendering to the window.
	 */
	Rectanglei getViewport()

	/**
	 * Return the width of the window.
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
	 * Switch between vertical sync being anabled/disabled.
	 */
	void toggleVSync()

	/**
	 * Complex rendering method for providing each of the scene, post-processing,
	 * UI, and completion steps ({@code swapBuffers}, {@code pollEvents}).  Use
	 * this method if you need full control of the render pipeline.
	 */
	RenderPipeline useRenderPipeline()

	/**
	 * Simple rendering method for using the window as the render target.  The
	 * closure will be surrounded with the necessary {@code clear}, {@code swapBuffers},
	 * and {@code pollEvents} calls.
	 *
	 * <p>For more complicated rendering requirements, look to {@link #useRenderPipeline}
	 * instead.
	 */
	void useWindow(Closure closure)

	/**
	 * Set the background colour of the window.
	 */
	Window withBackgroundColour(Colour colour)

	/**
	 * Convenience method to perform the actions of the closure with the OpenGL
	 * context, so that rendering commands can be executed in the current thread.
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
	 * Enable double-clicking the window to toggle fullscreen (Windows only).
	 * @return
	 */
	Window withDoubleClickForFullscreen()

	/**
	 * Maximize the window
	 */
	Window withMaximized()

	/**
	 * Set whether vsync is enabled/disabled for this window.
	 */
	Window withVSync(boolean vsync)
}
