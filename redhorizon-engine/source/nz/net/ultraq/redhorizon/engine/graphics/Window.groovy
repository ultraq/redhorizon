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

package nz.net.ultraq.redhorizon.engine.graphics

import nz.net.ultraq.redhorizon.engine.geometry.Dimension
import nz.net.ultraq.redhorizon.engine.input.InputSource

/**
 * The window device/handle, and related operations.
 *
 * @author Emanuel Rabina
 */
abstract class Window implements AutoCloseable, InputSource {

	/**
	 * Return the current window handle.
	 *
	 * @return
	 */
	abstract long getHandle()

	/**
	 * Return the current framebuffer dimensions.
	 *
	 * @return
	 */
	abstract Dimension getFramebufferSize()

	/**
	 * Returns the DPI scaling value of the current monitor.
	 *
	 * @return
	 */
	abstract float getMonitorScale()

	/**
	 * Returns the current internal rendering resolution.
	 *
	 * @return
	 */
	abstract Dimension getRenderResolution()

	/**
	 * Return the current window dimensions.
	 *
	 * @return
	 */
	abstract Dimension getSize()

	/**
	 * Returns the current output rendering resolution.
	 *
	 * @return
	 */
	abstract Dimension getTargetResolution()

	/**
	 * Communicate with the window so we're not locking up.
	 */
	abstract void pollEvents()

	/**
	 * Return whether or not the underlying window has signalled to be closed.
	 *
	 * @return
	 */
	abstract boolean shouldClose()

	/**
	 * Manually set whether or not the underlying window should close.
	 *
	 * @param close
	 */
	abstract void shouldClose(boolean close)

	/**
	 * Swap between the front and back buffers, pushing the new frame to the
	 * display.
	 */
	abstract void swapBuffers()

	/**
	 * Switch between windowed and fullscreen modes.
	 */
	abstract void toggleFullScreen()

	/**
	 * Switch between vertical sync being anabled/disabled.
	 */
	abstract void toggleVsync()
}
