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
import nz.net.ultraq.redhorizon.engine.GameTime
import nz.net.ultraq.redhorizon.engine.KeyEvent
import nz.net.ultraq.redhorizon.engine.WithGameClock
import nz.net.ultraq.redhorizon.engine.graphics.Colours
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.WithGraphicsEngine
import nz.net.ultraq.redhorizon.filetypes.ImagesFile
import nz.net.ultraq.redhorizon.filetypes.Palette
import nz.net.ultraq.redhorizon.utilities.unitviewer.Infantry
import nz.net.ultraq.redhorizon.utilities.unitviewer.Structure
import nz.net.ultraq.redhorizon.utilities.unitviewer.UnitData
import nz.net.ultraq.redhorizon.utilities.unitviewer.Vehicle

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

import groovy.json.JsonOutput
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

	@Parameters(index = '0', description = 'Path to the unit shp file to view, or a mix file that contains the unit')
	File file

	@Parameters(index = '1', arity = '0..1', description = 'If <file> is a mix file, this is the name of the shp in the mix file to view')
	String entryName

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

		logger.info('Loading {}...', file)
		def shpFile
		def unitId
		if (file.name.endsWith('.mix')) {
			new MixFile(file).withCloseable { mix ->
				def entry = mix.getEntry(entryName)
				if (entry) {
					logger.info('Loading {}...', entryName)
					shpFile = mix.getEntryData(entry).withStream { inputStream -> new ShpFile(inputStream) }
					unitId = entryName[0..<-4]
				}
				else {
					logger.error('{} not found in {}', entryName, file)
					throw new IllegalArgumentException()
				}
			}
		}
		else {
			shpFile = file.withInputStream { inputStream -> new ShpFile(inputStream) }
			unitId = file.name[0..<-4]
		}

		def configFileStream = this.class.classLoader.getResourceAsStream(
			"nz/net/ultraq/redhorizon/utilities/unitviewer/configurations/${unitId.toLowerCase()}.json")
		if (!configFileStream) {
			logger.error('No configuration available for {}', unitId)
			throw new IllegalArgumentException()
		}

		view(shpFile, configFileStream.text)

		return 0
	}

	/**
	 * Display the unit.
	 * 
	 * @param shpFile
	 * @param unitConfig
	 */
	private void view(ShpFile shpFile, String unitConfig) {

		logger.info('File details: {}', shpFile)
		logger.info('Configuration data:\n{}', JsonOutput.prettyPrint(unitConfig))

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

		def palette = new BufferedInputStream(this.class.classLoader.getResourceAsStream(paletteType.file)).withCloseable { inputStream ->
			return new PalFile(inputStream).withAlphaMask()
		}
		def config = new GraphicsConfiguration(
			clearColour: Colours.WHITE
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
