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

package nz.net.ultraq.redhorizon.utilities.objectviewer

import nz.net.ultraq.redhorizon.classic.filetypes.pal.PalFile
import nz.net.ultraq.redhorizon.classic.filetypes.shp.ShpFile
import nz.net.ultraq.redhorizon.engine.GameTime
import nz.net.ultraq.redhorizon.engine.KeyEvent
import nz.net.ultraq.redhorizon.engine.WithGameClock
import nz.net.ultraq.redhorizon.engine.graphics.Colour
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.WithGraphicsEngine
import nz.net.ultraq.redhorizon.filetypes.ImagesFile
import nz.net.ultraq.redhorizon.filetypes.Palette
import nz.net.ultraq.redhorizon.classic.PaletteTypes
import nz.net.ultraq.redhorizon.utilities.objectviewer.units.Infantry
import nz.net.ultraq.redhorizon.utilities.objectviewer.units.Structure
import nz.net.ultraq.redhorizon.utilities.objectviewer.units.UnitData
import nz.net.ultraq.redhorizon.utilities.objectviewer.units.Vehicle

import org.joml.Rectanglef
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UP
import static org.lwjgl.glfw.GLFW.GLFW_PRESS
import static org.lwjgl.glfw.GLFW.GLFW_REPEAT

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.TupleConstructor
import java.util.concurrent.Executors

/**
 * A unit viewer for testing rendering and unit configuration.
 * 
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class UnitViewer implements WithGameClock, WithGraphicsEngine {

	private static final Logger logger = LoggerFactory.getLogger(UnitViewer)

	final ShpFile shpFile
	final String unitId
	final PaletteTypes paletteType

	/**
	 * Display the unit.
	 */
	void view() {

		logger.info('File details: {}', shpFile)

		def unitConfig
		try {
			unitConfig = getResourceAsStream(
				"nz/net/ultraq/redhorizon/utilities/objectviewer/configurations/${unitId.toLowerCase()}.json")
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
		def config = new GraphicsConfiguration(
			clearColour: Colour.WHITE
		)

		Executors.newCachedThreadPool().executeAndShutdown { executorService ->
			withGameClock(executorService) { gameClock ->
				withGraphicsEngine(executorService, config) { graphicsEngine ->

					// Add the unit to the engine
					def unitCoordinates = new Rectanglef(0, 0, shpFile.width * 2, shpFile.height * 2).center()
					def unit = targetClass
						.getDeclaredConstructor(UnitData, ImagesFile, Palette, Rectanglef, GameTime)
						.newInstance(unitData, shpFile, palette, unitCoordinates, gameClock)
					graphicsEngine.addSceneElement(unit)

					logger.info('Displaying the image in another window.  Close the window to exit.')

					// Key event handler
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
								case GLFW_KEY_ESCAPE:
									graphicsEngine.stop()
									break
							}
						}
					}
				}
			}
		}
	}
}
