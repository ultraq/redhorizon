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

import nz.net.ultraq.redhorizon.classic.Faction
import nz.net.ultraq.redhorizon.explorer.PaletteType
import nz.net.ultraq.redhorizon.graphics.Palette
import nz.net.ultraq.redhorizon.graphics.PaletteAlphaMask
import nz.net.ultraq.redhorizon.graphics.PaletteSwapMap
import nz.net.ultraq.redhorizon.scene.Node
import nz.net.ultraq.redhorizon.script.Script
import nz.net.ultraq.redhorizon.script.ScriptNode

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.*

/**
 * The palette used for any paletted graphics in the scene.
 *
 * @author Emanuel Rabina
 */
class GlobalPalette extends Node<GlobalPalette> {

	private static final Logger logger = LoggerFactory.getLogger(GlobalPalette)

	private Palette palette
	private PaletteType paletteType
	private PaletteSwapMap paletteSwapMap
	private Faction faction = Faction.GOLD

	/**
	 * Constructor, create the global palette and load an initial palette.
	 */
	GlobalPalette() {

		palette = addAndReturnChild(loadPalette())
		addChild(new PaletteAlphaMask())
		paletteSwapMap = addAndReturnChild(new PaletteSwapMap(faction.colourIndexes))
		addChild(new ScriptNode(GlobalPaletteScript))
	}

	/**
	 * Cycle through available faction colours, replacing the current global one.
	 */
	void cycleFaction() {

		faction++
		paletteSwapMap.setColourIndexes(faction.colourIndexes)
		logger.info('Viewing with {} faction colours', faction.name())
	}

	/**
	 * Cycle through the available palettes, replacing the current global one.
	 */
	void cyclePalette() {

		scene.queueUpdate { ->
			palette.remove().close()
			palette = addAndReturnChild(loadPalette(paletteType.next()))
			logger.info('Using {} palette', paletteType.name())
		}
	}

	/**
	 * Load the given palette as the global palette for objects.
	 */
	private Palette loadPalette(PaletteType paletteType = PaletteType.RA_TEMPERATE) {

		logger.info("Using ${paletteType} palette")
		this.paletteType = paletteType
		return getResourceAsStream(paletteType.file).withBufferedStream { stream ->
			return new Palette(paletteType.file, stream)
		}
	}

	/**
	 * Reset the faction colours to the default.
	 */
	void resetFaction() {

		faction = Faction.GOLD
		paletteSwapMap.setColourIndexes(faction.colourIndexes)
	}

	/**
	 * Script for adjusting the global palette.
	 */
	static class GlobalPaletteScript extends Script<GlobalPalette> {

		@Override
		void update(float delta) {

			if (input.keyPressed(GLFW_KEY_F, true)) {
				node.cycleFaction()
			}
			else if (input.keyPressed(GLFW_KEY_P, true)) {
				node.cyclePalette()
			}
		}
	}
}
