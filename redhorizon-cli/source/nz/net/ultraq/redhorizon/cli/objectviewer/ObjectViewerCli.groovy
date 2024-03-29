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

import nz.net.ultraq.redhorizon.classic.filetypes.IniFile
import nz.net.ultraq.redhorizon.classic.filetypes.ShpFile
import nz.net.ultraq.redhorizon.cli.FileOptions
import nz.net.ultraq.redhorizon.cli.GraphicsOptions
import nz.net.ultraq.redhorizon.cli.PaletteOptions
import nz.net.ultraq.redhorizon.engine.geometry.Dimension
import nz.net.ultraq.redhorizon.engine.graphics.Colour
import nz.net.ultraq.redhorizon.engine.resources.ResourceManager

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine.Command
import picocli.CommandLine.Mixin
import picocli.CommandLine.Model.CommandSpec
import picocli.CommandLine.Option
import picocli.CommandLine.Spec

import java.util.concurrent.Callable

/**
 * A game object viewer for testing their rendering and configuration.
 *
 * @author Emanuel Rabina
 */
@Command(
	name = "view",
	header = [
		'',
		'Red Horizon Object Viewer',
		'=========================',
		''
	],
	mixinStandardHelpOptions = true
)
class ObjectViewerCli implements Callable<Integer> {

	private static final Logger logger = LoggerFactory.getLogger(ObjectViewerCli)

	@Spec
	CommandSpec commandSpec

	@Mixin
	FileOptions fileOptions

	@Mixin
	GraphicsOptions graphicsOptions

	@Mixin
	PaletteOptions paletteOptions

	@Option(names = ['--touchpad-input'], description = 'Use touchpad scroll gestures to navigate')
	boolean touchpadInput

	/**
	 * Launch the unit viewer.
	 *
	 * @return
	 */
	@Override
	Integer call() {

		Thread.currentThread().name = 'Object Viewer [main]'
		logger.info('Red Horizon Object Viewer {}', commandSpec.parent().version()[0])

		fileOptions.useFile(logger) { objectFile ->
			switch (objectFile) {
				case ShpFile:
					def objectId = fileOptions.entryName?.nameWithoutExtension ?: fileOptions.file.nameWithoutExtension
					def graphicsConfig = graphicsOptions.asGraphicsConfiguration(
						clearColour: Colour.GREY
					)
					new UnitViewer(graphicsConfig, objectFile, objectId, paletteOptions.loadPalette(true)).start()
					break
				case IniFile:
					def graphicsConfig = graphicsOptions.asGraphicsConfiguration(
						renderResolution: new Dimension(1280, 800)
					)
					// Assume the directory in which file resides is where we can search for items
					new ResourceManager(fileOptions.file.parentFile,
						'nz.net.ultraq.redhorizon.filetypes',
						'nz.net.ultraq.redhorizon.classic.filetypes').withCloseable { resourceManager ->
						new MapViewer(graphicsConfig, resourceManager, objectFile, touchpadInput).start()
					}
					break
				default:
					logger.error('No viewer for the associated file class of {}', objectFile)
					throw new UnsupportedOperationException()
			}
		}

		return 0
	}
}
