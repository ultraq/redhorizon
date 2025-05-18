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

import nz.net.ultraq.redhorizon.engine.geometry.Dimension

import static Colour.BLACK

import groovy.transform.MapConstructor
import groovy.transform.ToString

/**
 * Graphics configuration object.
 *
 * @author Emanuel Rabina
 */
@MapConstructor(noArg = true)
@ToString(includeNames = true, includePackage = false)
class GraphicsConfiguration {

	final Colour clearColour = BLACK

	/**
	 * Enable debugging checks and logging.
	 */
	final boolean debug = true

	/**
	 * Use the entire screen instead of a floating window.
	 */
	final boolean fullScreen

	/**
	 * Have the window take up the entire screen.
	 */
	final boolean maximized

	/**
	 * The rendering resolution to use, before post-processing effects.
	 */
	final Dimension renderResolution = new Dimension(640, 400)

	/**
	 * Whether or not to start the application with the ImGui chrome.
	 */
	final boolean startWithChrome

	// The aspect ratio of a 320x200 image on VGA screens with non-square pixels
	private static final float ASPECT_RATIO_VGA = 4 / 3

	// The aspect ratio of a 320x200 image on modern displays
	private static final float ASPECT_RATIO_MODERN = 16 / 10

	/**
	 * The target aspect ratio for rendering given the current settings.
	 */
	final float targetAspectRatio = ASPECT_RATIO_MODERN

	final boolean vsync = true
}
