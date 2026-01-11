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

import nz.net.ultraq.redhorizon.classic.graphics.AlphaMaskComponent
import nz.net.ultraq.redhorizon.classic.graphics.PaletteComponent
import nz.net.ultraq.redhorizon.engine.Entity
import nz.net.ultraq.redhorizon.engine.scripts.EntityScript
import nz.net.ultraq.redhorizon.engine.scripts.ScriptComponent
import nz.net.ultraq.redhorizon.explorer.PaletteType
import nz.net.ultraq.redhorizon.explorer.ui.CyclePaletteEvent
import nz.net.ultraq.redhorizon.graphics.Palette

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.GLFW_KEY_P

/**
 * The palette used for any paletted graphics in the scene.
 *
 * @author Emanuel Rabina
 */
class GlobalPalette extends Entity<GlobalPalette> {

	private static final Logger logger = LoggerFactory.getLogger(GlobalPalette)
	private PaletteType currentPalette

	/**
	 * Constructor, create the global palette and load an initial palette.
	 */
	GlobalPalette() {

		addComponent(new PaletteComponent(loadPalette()))
		addComponent(new AlphaMaskComponent())
		addComponent(new ScriptComponent(GlobalPaletteScript))
	}

	/**
	 * Cycle through the available palettes, replacing the current global one.
	 */
	private void cyclePalette() {

		var paletteComponent = findComponentByType(PaletteComponent)
		paletteComponent.palette.close()
		paletteComponent.palette = loadPalette(currentPalette.next())
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

	/**
	 * Update the global palette in response to inputs/events.
	 */
	static class GlobalPaletteScript extends EntityScript<GlobalPalette> {

		@Override
		void init() {

			entity.scene.on(CyclePaletteEvent) { event ->
				entity.scene.queueUpdate { ->
					entity.cyclePalette()
				}
			}
		}

		@Override
		void update(float delta) {

			if (input.keyPressed(GLFW_KEY_P, true)) {
				entity.cyclePalette()
			}
		}
	}
}
