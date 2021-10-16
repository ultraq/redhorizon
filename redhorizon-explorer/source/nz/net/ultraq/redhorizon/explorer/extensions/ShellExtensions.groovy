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

package nz.net.ultraq.redhorizon.explorer.extensions

import org.eclipse.swt.graphics.Rectangle
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Shell

/**
 * Extensions to the {@link Shell} class.
 * 
 * @author Emanuel Rabina
 */
class ShellExtensions {

	/**
	 * Open the shell in the center of the display.
	 * 
	 * @param self
	 * @param display
	 */
	static void openCentered(Shell self, Display display) {

		// Center on screen and open
		self.pack()
		def resolution = display.primaryMonitor.bounds
		def window = self.bounds
		self.bounds = new Rectangle(
			(resolution.width >> 1)  - (window.width >> 1),
			(resolution.height >> 1) - (window.height >> 1),
			window.width, window.height)
		self.open()
	}
}
