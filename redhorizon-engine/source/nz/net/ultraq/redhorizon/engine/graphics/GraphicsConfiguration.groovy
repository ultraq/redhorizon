/* 
 * Copyright 2020, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import static Colour.BLACK

import groovy.transform.MapConstructor
import groovy.transform.ToString

/**
 * Graphics configuration object.
 * 
 * @author Emanuel Rabina
 */
@MapConstructor(excludes = ['debug'], noArg = true)
@ToString(includeNames = true, includePackage = false)
class GraphicsConfiguration {

	final Colour clearColour = BLACK

	/**
	 * Enable debugging checks and logging.  Is always {@code true} during
	 * development.
	 */
	final boolean debug = true

	/**
	 * Adjust the aspect ratio of visual elements for today's displays, given that
	 * a lot of the graphics of the time were on CRT displays using resolutions
	 * that resulted in tall pixels.
	 */
	final boolean fixAspectRatio

	/**
	 * Use the entire screen instead of a floating window.
	 */
	final boolean fullScreen

	/**
	 * Include a scanline effect in what's rendered to the screen.
	 */
	final boolean scanlines
}
