/*
 * Copyright 2026, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.explorer.objects

import nz.net.ultraq.redhorizon.classic.graphics.AlphaMask
import nz.net.ultraq.redhorizon.explorer.PaletteType
import nz.net.ultraq.redhorizon.graphics.Palette
import nz.net.ultraq.redhorizon.scenegraph.Node

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * The palette used for any paletted graphics in the scene.
 *
 * @author Emanuel Rabina
 */
class GlobalPalette extends Node<GlobalPalette> {

	private static final Logger logger = LoggerFactory.getLogger(GlobalPalette)
	private PaletteType currentPalette

	/**
	 * Constructor, create the global palette and load an initial palette.
	 */
	GlobalPalette() {

		addChild(loadPalette())
		addChild(new AlphaMask())
	}

	/**
	 * Cycle through the available palettes, replacing the current global one.
	 */
	void cyclePalette() {

		var palette = findDescendentByType(Palette)
		removeChild(palette)
		palette.close()
		addChild(loadPalette(currentPalette.next()))
	}

	/**
	 * Load the given palette as the global palette for objects.
	 */
	private Palette loadPalette(PaletteType paletteType = PaletteType.RA_TEMPERATE) {

		logger.info("Using ${paletteType} palette")
		currentPalette = paletteType
		return getResourceAsStream(paletteType.file).withBufferedStream { stream ->
			return new Palette(paletteType.file, stream)
		}
	}
}
