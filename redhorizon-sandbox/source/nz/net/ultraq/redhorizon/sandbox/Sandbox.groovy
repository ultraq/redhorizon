/*
 * Copyright 2023, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.sandbox

import nz.net.ultraq.redhorizon.classic.filetypes.IniFile
import nz.net.ultraq.redhorizon.classic.filetypes.MapFile
import nz.net.ultraq.redhorizon.classic.filetypes.PalFile
import nz.net.ultraq.redhorizon.classic.maps.Map
import nz.net.ultraq.redhorizon.engine.Application
import nz.net.ultraq.redhorizon.engine.geometry.Dimension
import nz.net.ultraq.redhorizon.engine.graphics.Colour
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.resources.ResourceManager
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.explorer.PaletteType
import nz.net.ultraq.redhorizon.explorer.objects.GridLines
import nz.net.ultraq.redhorizon.explorer.scripts.MapViewerScript
import nz.net.ultraq.redhorizon.filetypes.Palette

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Main class to run a play and testing environment for any game dev stuff I
 * wanna try out.
 *
 * @author Emanuel Rabina
 */
class Sandbox {

	private static final Logger logger = LoggerFactory.getLogger(Sandbox)
	private static final String mapFileName = 'scr01ea.ini'

	final Palette palette
	final boolean touchpadInput

	private final ResourceManager resourceManager = new ResourceManager(
		new File('mix/red-alert'),
		'nz.net.ultraq.redhorizon.filetypes',
		'nz.net.ultraq.redhorizon.classic.filetypes')

	static void main(String[] args) {
		new Sandbox(args.length > 0 && args[0] == '--touchpad-input')
		System.exit(0)
	}

	/**
	 * Constructor, build the sandbox.
	 */
	Sandbox(boolean touchpadInput) {

		palette = getResourceAsStream(PaletteType.RA_TEMPERATE.file).withBufferedStream { inputStream ->
			return new PalFile(inputStream)
		}
		this.touchpadInput = touchpadInput

		new Application('Sandbox', '0.1.0')
			.addAudioSystem()
			.addGraphicsSystem(new GraphicsConfiguration(
				clearColour: Colour.GREY,
				renderResolution: new Dimension(1280, 800)
			))
			.addTimeSystem()
			.onApplicationStart(this::applicationStart)
			.onApplicationStop(this::applicationStop)
			.start()
	}

	private void applicationStart(Application application, Scene scene) {

		scene << new GridLines()

		logger.info('Loading sandbox map, {}', mapFileName)

		var mapFile = getResourceAsStream(mapFileName).withBufferedStream { new IniFile(it) as MapFile }
		scene << new Map(mapFile, resourceManager).attachScript(new MapViewerScript(touchpadInput))

	}

	private void applicationStop(Application application, Scene scene) {

		scene.clear()
		resourceManager.close()
	}
}
