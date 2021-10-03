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

import nz.net.ultraq.redhorizon.classic.PaletteTypes
import nz.net.ultraq.redhorizon.classic.filetypes.pal.PalFile
import nz.net.ultraq.redhorizon.classic.filetypes.shp.ShpFile
import nz.net.ultraq.redhorizon.cli.objectviewer.units.Infantry
import nz.net.ultraq.redhorizon.cli.objectviewer.units.Structure
import nz.net.ultraq.redhorizon.cli.objectviewer.units.Vehicle
import nz.net.ultraq.redhorizon.cli.objectviewer.units.UnitData
import nz.net.ultraq.redhorizon.engine.GameTime
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.WindowCreatedEvent
import nz.net.ultraq.redhorizon.engine.input.KeyEvent
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

	final ShpFile shpFile
	final String unitId
	final PaletteTypes paletteType
	final boolean touchpadInput

	/**
	 * Constructor, set the unit to be displayed.
	 * 
	 * @param graphicsConfig
	 * @param shpFile
	 * @param unitId
	 * @param paletteType
	 * @param touchpadInput
	 */
	UnitViewer(GraphicsConfiguration graphicsConfig, ShpFile shpFile, String unitId, PaletteTypes paletteType, boolean touchpadInput) {

		super(null, graphicsConfig)
		this.shpFile = shpFile
		this.unitId = unitId
		this.paletteType = paletteType
		this.touchpadInput = touchpadInput
	}

	@Override
	void run() {

		logger.info('File details: {}', shpFile)

		def unitConfig
		try {
			unitConfig = getResourceAsStream(
				"nz/net/ultraq/redhorizon/cli/objectviewer/configurations/${unitId.toLowerCase()}.json")
				.withBufferedStream { inputStream ->
					return inputStream.text
				}
			logger.info('Configuration data:\n{}', JsonOutput.prettyPrint(unitConfig))
		}
		catch (IllegalArgumentException ignored) {
			logger.error('No configuration available for {}', unitId)
			throw new IllegalArgumentException()
		}

		def unitData = new JsonSlurper().parseText(unitConfig) as UnitData
		def targetClass
		switch (unitData.type) {
			case 'infantry':
				targetClass = Infantry
				break
			case 'vehicle':
				targetClass = Vehicle
				break
			case 'structure':
				targetClass = Structure
				break
			default:
				throw new UnsupportedOperationException("Unit type ${unitData.type} not supported")
		}

		def palette = getResourceAsStream(paletteType.file).withBufferedStream { inputStream ->
			return new PalFile(inputStream).withAlphaMask()
		}

		graphicsEngine.on(WindowCreatedEvent) { event ->
			graphicsEngine.camera.scale(2)
		}

		// Add the unit to the engine
		def unit = targetClass
			.getDeclaredConstructor(UnitData, ImagesFile, Palette, GameTime)
			.newInstance(unitData, shpFile, palette, gameClock)
			.translate(-shpFile.width / 2, -shpFile.height / 2, 0)
		scene << unit

		logger.info('Displaying the image in another window.  Close the window to exit.')

		applyViewerInputs(inputEventStream, graphicsEngine, touchpadInput)

		// Custom inputs
		graphicsEngine.on(KeyEvent) { event ->
			if (event.action == GLFW_PRESS || event.action == GLFW_REPEAT) {
				switch (event.key) {
					case GLFW_KEY_LEFT:
						unit.rotateLeft()
						break
					case GLFW_KEY_RIGHT:
						unit.rotateRight()
						break
					case GLFW_KEY_UP:
						unit.previousAnimation()
						break
					case GLFW_KEY_DOWN:
						unit.nextAnimation()
						break
					case GLFW_KEY_SPACE:
						gameClock.togglePause()
						break
				}
			}
		}
	}
}
