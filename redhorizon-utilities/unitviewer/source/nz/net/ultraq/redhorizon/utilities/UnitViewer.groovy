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

import nz.net.ultraq.redhorizon.classic.filetypes.pal.PalFile
import nz.net.ultraq.redhorizon.engine.graphics.WindowCreatedEvent
import nz.net.ultraq.redhorizon.engine.graphics.WithGraphicsEngine
import nz.net.ultraq.redhorizon.utilities.unitviewer.Unit
import nz.net.ultraq.redhorizon.utilities.unitviewer.UnitConfigs
import nz.net.ultraq.redhorizon.utilities.unitviewer.UnitData

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Model.CommandSpec
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import picocli.CommandLine.Spec

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
class UnitViewer implements Callable<Integer>, WithGraphicsEngine {

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
		
		def unitData = new JsonSlurper().parse(this.class.classLoader.getResourceAsStream(unitConfig.file)) as UnitData
		view(unitData)

		return 0
	}

	/**
	 * Display the unit.
	 * 
	 * @param unitData
	 */
	private void view(UnitData unitData) {

		def palette = new BufferedInputStream(this.class.classLoader.getResourceAsStream(paletteType.file)).withCloseable { inputStream ->
			return new PalFile(inputStream)
		}

		Executors.newCachedThreadPool().executeAndShutdown { executorService ->
			withGraphicsEngine(executorService, false) { graphicsEngine ->
				def unit = new Unit(unitData, palette)

				// Add the unit to the engine once we have the window dimensions
				graphicsEngine.on(WindowCreatedEvent) { event ->
					graphicsEngine.addSceneElement(unit)
				}

				logger.info('Displaying the image in another window.  Close the window to exit.')
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
