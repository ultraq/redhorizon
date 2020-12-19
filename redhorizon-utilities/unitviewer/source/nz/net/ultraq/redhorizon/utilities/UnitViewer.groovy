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

package nz.net.ultraq.redhorizon.utilities

import nz.net.ultraq.redhorizon.classic.filetypes.mix.MixFile
import nz.net.ultraq.redhorizon.classic.filetypes.pal.PalFile
import nz.net.ultraq.redhorizon.classic.filetypes.shp.ShpFile
import nz.net.ultraq.redhorizon.engine.KeyEvent
import nz.net.ultraq.redhorizon.engine.WithGameClock
import nz.net.ultraq.redhorizon.engine.graphics.Colours
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.WindowCreatedEvent
import nz.net.ultraq.redhorizon.engine.graphics.WithGraphicsEngine
import nz.net.ultraq.redhorizon.utilities.unitviewer.Unit
import nz.net.ultraq.redhorizon.utilities.unitviewer.UnitConfigs
import nz.net.ultraq.redhorizon.utilities.unitviewer.UnitData

import org.joml.Rectanglef
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Model.CommandSpec
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import picocli.CommandLine.Spec
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UP
import static org.lwjgl.glfw.GLFW.GLFW_PRESS
import static org.lwjgl.glfw.GLFW.GLFW_REPEAT

import groovy.json.JsonSlurper
import java.util.concurrent.Callable
import java.util.concurrent.Executors

/**
 * A unit and structure viewer for testing game object rendering and
 * configuration.
 * 
 * @author Emanuel Rabina
 */
@Command(
	name = "view",
	header = [
		'',
		'Red Horizon Unit Viewer',
		'=======================',
		''
	],
	mixinStandardHelpOptions = true,
	version = '${sys:redhorizon.version}'
)
class UnitViewer implements Callable<Integer>, WithGameClock, WithGraphicsEngine {

	private static final Logger logger = LoggerFactory.getLogger(UnitViewer)

	@Spec
	CommandSpec commandSpec

	@Parameters
	String unitName

	@Option(names = ['--palette'], defaultValue = 'ra', description = 'Which game palette to apply to a paletted image.  One of "RA" or "TD".')
	PaletteTypes paletteType

	/**
	 * Launch the unit viewer.
	 *
	 * @return
	 */
	@Override
	Integer call() {

		Thread.currentThread().name = 'Unit Viewer [main]'
		logger.info('Red Horizon Unit Viewer {}', commandSpec.version()[0] ?: '(development)')

		logger.info('Loading {}...', unitName)
		def unitConfig = UnitConfigs.valueOf(unitName.toUpperCase())
		if (!unitConfig) {
			logger.error('No configuration available for {}', unitName)
			throw new IllegalArgumentException()
		}

		def configData = this.class.classLoader.getResourceAsStream(unitConfig.file).text
		logger.info('Configuration data:\n{}', configData)

		def unitData = new JsonSlurper().parseText(configData) as UnitData
		view(unitData)

		return 0
	}

	/**
	 * Display the unit.
	 * 
	 * @param unitData
	 */
	private void view(UnitData unitData) {

		// TODO: Configure the path to the mix file, or do some kind of item lookup
		def mixFile = new MixFile(new File('mix/red-alert/Conquer.mix'))
		def mixFileEntry = mixFile.getEntry(unitData.shpFile.filename)
		def shpFile = mixFile.getEntryData(mixFileEntry).withStream { inputStream ->
			return new ShpFile(inputStream)
		}
		logger.info('{} details: {}', unitData.shpFile.filename, shpFile)

		def palette = new BufferedInputStream(this.class.classLoader.getResourceAsStream(paletteType.file)).withCloseable { inputStream ->
			return new PalFile(inputStream).withAlphaMask()
		}

		def config = new GraphicsConfiguration(
			clearColour: Colours.WHITE
		)

		Executors.newCachedThreadPool().executeAndShutdown { executorService ->
			withGameClock(executorService) { gameClock ->
				withGraphicsEngine(executorService, config) { graphicsEngine ->

					// Add the unit to the engine once we have the window dimensions
					Unit unit
					graphicsEngine.on(WindowCreatedEvent) { event ->
						def unitCoordinates = centerDimensions(new Rectanglef(0, 0, shpFile.width * 2, shpFile.height * 2))
						unit = new Unit(unitData, shpFile, palette, unitCoordinates, gameClock)
						graphicsEngine.addSceneElement(unit)
					}

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

	/**
	 * Bootstrap the application using Picocli.
	 *
	 * @param args
	 */
	static void main(String[] args) {
		System.exit(
			new CommandLine(new UnitViewer())
			.setCaseInsensitiveEnumValuesAllowed(true)
			.execute(args)
		)
	}
}
