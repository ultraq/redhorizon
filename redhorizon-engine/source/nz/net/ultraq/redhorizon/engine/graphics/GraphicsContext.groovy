/* 
 * Copyright 2021, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import nz.net.ultraq.redhorizon.engine.Context
import nz.net.ultraq.redhorizon.geometry.Dimension

/**
 * The graphics execution context, contains information about the target
 * rendering device.
 * 
 * @author Emanuel Rabina
 */
abstract class GraphicsContext extends Context {

	/**
	 * Returns the current internal rendering resolution.
	 * 
	 * @return
	 */
	abstract Dimension getRenderResolution()

	/**
	 * Returns the current output rendering resolution.
	 * 
	 * @return
	 */
	abstract Dimension getTargetResolution()

	/**
	 * Return the current window handle.
	 * 
	 * @return
	 */
	abstract long getWindow()

	/**
	 * Return the current window dimensions.
	 * 
	 * @return
	 */
	abstract Dimension getWindowSize()

	/**
	 * Return whether or not the underlying window has signalled to be closed.
	 * 
	 * @return
	 */
	abstract boolean windowShouldClose()

	/**
	 * Manually set whether or not the underlying window should close.
	 * 
	 * @param close
	 */
	abstract void windowShouldClose(boolean close)
}
