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

package nz.net.ultraq.redhorizon.cli

import nz.net.ultraq.redhorizon.engine.graphics.Colour
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration

import picocli.CommandLine.Option

import groovy.transform.NamedVariant

/**
 * Common graphical options.
 * 
 * @author Emanuel Rabina
 */
class GraphicsOptions {

	@Option(names = ['--full-screen'], description = 'Run in fullscreen mode')
	boolean fullScreen

	@Option(names = ['--maximized'], description = 'Use a maximized window')
	boolean maximized

	@Option(names = ['--scanlines'], description = 'Add scanlines to the image, emulating the look of images on CRT displays')
	boolean scanlines

	/**
	 * Create a {@code GraphicsConfiguration} object from the current settings.
	 * 
	 * @param clearColour
	 * @return
	 */
	@NamedVariant
	GraphicsConfiguration asGraphicsConfiguration(Colour clearColour = Colour.BLACK, boolean startWithChrome = false) {

		return new GraphicsConfiguration(
			clearColour: clearColour ?: Colour.BLACK,
			fullScreen: fullScreen,
			maximized: maximized,
			scanlines: scanlines,
			startWithChrome: startWithChrome ?: false
		)
	}
}
