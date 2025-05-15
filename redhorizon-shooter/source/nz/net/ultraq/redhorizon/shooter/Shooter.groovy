/*
 * Copyright 2024, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.shooter

import nz.net.ultraq.redhorizon.classic.filetypes.PalFile
import nz.net.ultraq.redhorizon.classic.maps.Map
import nz.net.ultraq.redhorizon.classic.nodes.GlobalPalette
import nz.net.ultraq.redhorizon.classic.nodes.Layer
import nz.net.ultraq.redhorizon.engine.geometry.Dimension
import nz.net.ultraq.redhorizon.engine.graphics.Colour
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.resources.ResourceManager
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.engine.scenegraph.nodes.Camera
import nz.net.ultraq.redhorizon.engine.scenegraph.nodes.GridLines
import nz.net.ultraq.redhorizon.explorer.PaletteType
import nz.net.ultraq.redhorizon.runtime.Application
import nz.net.ultraq.redhorizon.runtime.Runtime
import nz.net.ultraq.redhorizon.runtime.VersionReader
import nz.net.ultraq.redhorizon.shooter.objects.Player

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine
import picocli.CommandLine.Command

import java.util.concurrent.Callable

/**
 * A twin-stick shooter game to exercise the Red Horizon engine ✈️🔫
 *
 * @author Emanuel Rabina
 */
class Shooter implements Application {

	private static final Dimension RENDER_RESOLUTION = new Dimension(720, 405)
	private static final Logger logger = LoggerFactory.getLogger(Shooter)

	/**
	 * Entry point to the Shooter game.
	 */
	static void main(String[] args) {

		System.exit(new CommandLine(new CliWrapper()).execute(args))
	}

	/**
	 * Tiny CLI wrapper around the Shooter game so it's launchable w/ Picocli.
	 */
	@Command(name = 'shooter')
	static class CliWrapper implements Callable<Integer> {

		@Override
		Integer call() {

			return new Runtime(new Shooter())
				.withGraphicsConfiguration(new GraphicsConfiguration(
					clearColour: Colour.GREY,
					renderResolution: RENDER_RESOLUTION
				))
				.execute()
		}
	}

	final String name = 'Shooter'
	final String version = new VersionReader('shooter.properties').read()

	@Override
	void start(Scene scene) {

		new ResourceManager(
			new File(System.getProperty('user.dir'), 'mix'),
			'nz.net.ultraq.redhorizon.filetypes',
			'nz.net.ultraq.redhorizon.classic.filetypes').withCloseable { resourceManager ->

			var camera = new Camera(RENDER_RESOLUTION)
			scene << camera

			scene << new GridLines(Map.MAX_BOUNDS, 24)

			var paletteType = PaletteType.RA_TEMPERATE
			logger.info('Using {} palette', paletteType)
			var globalPalette = getResourceAsStream(paletteType.file).withBufferedStream { inputStream ->
				return new GlobalPalette(new PalFile(inputStream))
			}
			scene << globalPalette

			var player = new Player(resourceManager).tap {
				layer = Layer.SPRITES_UPPER
			}
			scene << player

			camera.follow(player)

//			var mapFile = resourceManager.loadFile('scm01ea.ini', IniFile)
//			var map = new Map(mapFile as MapFile, resourceManager)
//			scene << map
		}
	}

	@Override
	void stop(Scene scene) {

		scene.clear()
	}
}
