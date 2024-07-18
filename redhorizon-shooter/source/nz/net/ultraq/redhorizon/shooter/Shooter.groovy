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
import nz.net.ultraq.redhorizon.classic.nodes.GlobalPalette
import nz.net.ultraq.redhorizon.engine.Application
import nz.net.ultraq.redhorizon.engine.geometry.Dimension
import nz.net.ultraq.redhorizon.engine.graphics.Colour
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.resources.ResourceManager
import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.engine.scenegraph.nodes.Camera
import nz.net.ultraq.redhorizon.engine.scenegraph.nodes.GridLines
import nz.net.ultraq.redhorizon.explorer.PaletteType
import nz.net.ultraq.redhorizon.filetypes.Palette
import nz.net.ultraq.redhorizon.shooter.objects.Player

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Main class for the shooter game ✈️🔫
 *
 * @author Emanuel Rabina
 */
class Shooter {

	private static final Dimension RENDER_RESOLUTION = new Dimension(720, 405)
	private static final Logger logger = LoggerFactory.getLogger(Shooter)

	private GlobalPalette globalPalette
	private Camera camera
	private Node player

	/**
	 * Constructor, launch the shooter game.
	 */
	Shooter(String version) {

		new Application('Shooter', version)
			.addGraphicsSystem(new GraphicsConfiguration(
				clearColour: Colour.GREY,
				renderResolution: RENDER_RESOLUTION
			))
			.addTimeSystem()
			.onApplicationStart { application, scene -> start(scene) }
			.onApplicationStop { application, scene -> stop(scene) }
			.start()
	}

	/**
	 * Set up and begin the game.
	 */
	private void start(Scene scene) {

		new ResourceManager(
			new File(System.getProperty('user.dir'), 'mix'),
			'nz.net.ultraq.redhorizon.filetypes',
			'nz.net.ultraq.redhorizon.classic.filetypes').withCloseable { resourceManager ->

			camera = new Camera(RENDER_RESOLUTION)
			scene << camera

			scene << new GridLines(-RENDER_RESOLUTION.width() / 2 as int, RENDER_RESOLUTION.width() / 2 as int, 24)

			globalPalette = new GlobalPalette(loadPalette())
			scene << globalPalette

			player = new Player(resourceManager)
			scene << player

			camera.follow(player)
		}
	}

	/**
	 * Load the given palette as the global palette for objects.
	 */
	private Palette loadPalette(PaletteType paletteType = PaletteType.RA_TEMPERATE) {

		logger.info("Using ${paletteType} palette")
		return getResourceAsStream(paletteType.file).withBufferedStream { inputStream ->
			return new PalFile(inputStream)
		}
	}

	/**
	 * Clean up.
	 */
	private void stop(Scene scene) {

		scene.clear()
	}
}
