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

package nz.net.ultraq.redhorizon.cli.objectviewer

import nz.net.ultraq.redhorizon.classic.filetypes.ShpFile
import nz.net.ultraq.redhorizon.cli.objectviewer.units.Faction
import nz.net.ultraq.redhorizon.cli.objectviewer.units.Infantry
import nz.net.ultraq.redhorizon.cli.objectviewer.units.Structure
import nz.net.ultraq.redhorizon.cli.objectviewer.units.UnitData
import nz.net.ultraq.redhorizon.cli.objectviewer.units.Vehicle
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.input.KeyControl
import nz.net.ultraq.redhorizon.filetypes.ImagesFile
import nz.net.ultraq.redhorizon.filetypes.Palette

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.*

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

/**
 * A unit viewer for testing rendering and unit configuration.
 *
 * @author Emanuel Rabina
 */
class UnitViewer extends Viewer {

	private static final Logger logger = LoggerFactory.getLogger(UnitViewer)

	final float initialScale = 2.0f
	final float[] scaleRange = (1.0..4.0).by(0.1)

	final ShpFile shpFile
	final String unitId
	final Palette palette

	/**
	 * Constructor, set the unit to be displayed.
	 *
	 * @param graphicsConfig
	 * @param shpFile
	 * @param unitId
	 * @param palette
	 */
	UnitViewer(GraphicsConfiguration graphicsConfig, ShpFile shpFile, String unitId, Palette palette) {

		super(null, graphicsConfig)
		this.shpFile = shpFile
		this.unitId = unitId
		this.palette = palette
	}

	@Override
	protected void applicationStart() {

		super.applicationStart()
		logger.info('File details: {}', shpFile)

		def unitConfig
		try {
			unitConfig = getResourceAsText("nz/net/ultraq/redhorizon/cli/objectviewer/configurations/${unitId.toLowerCase()}.json")
			logger.info('Configuration data:\n{}', JsonOutput.prettyPrint(unitConfig))
		}
		catch (IllegalArgumentException ignored) {
			logger.error('No configuration available for {}', unitId)
			throw new IllegalArgumentException()
		}

		def unitData = new JsonSlurper().parseText(unitConfig) as UnitData
		def targetClass = switch (unitData.type) {
			case 'infantry' -> Infantry
			case 'vehicle' -> Vehicle
			case 'structure' -> Structure
			default -> throw new UnsupportedOperationException("Unit type ${unitData.type} not supported")
		}

		// Add the unit to the engine
		def unit = targetClass
			.getDeclaredConstructor(UnitData, ImagesFile, Palette)
			.newInstance(unitData, shpFile, palette)
			.translate(-shpFile.width / 2, -shpFile.height / 2, 0)
		scene << unit

		logger.info('Displaying the image in another window.  Close the window to exit.')

		// Custom inputs
		inputEventStream.addControl(new KeyControl(GLFW_KEY_LEFT, 'Rotate left', { ->
			unit.rotateLeft()
		}))
		inputEventStream.addControl(new KeyControl(GLFW_KEY_RIGHT, 'Rotate right', { ->
			unit.rotateRight()
		}))
		inputEventStream.addControl(new KeyControl(GLFW_KEY_UP, 'Previous animation', { ->
			unit.previousAnimation()
		}))
		inputEventStream.addControl(new KeyControl(GLFW_KEY_DOWN, 'Next animation', { ->
			unit.nextAnimation()
		}))
		inputEventStream.addControl(new KeyControl(GLFW_KEY_SPACE, 'Pause animation', { ->
			gameClock.togglePause()
		}))
		final Faction[] factions = Faction.values()
		inputEventStream.addControl(new KeyControl(GLFW_KEY_P, 'Cycle faction colours', { ->
			unit.faction = factions[(unit.faction.ordinal() + 1) % factions.length]
		}))
	}
}
