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

import nz.net.ultraq.redhorizon.engine.Application
import nz.net.ultraq.redhorizon.engine.geometry.Dimension
import nz.net.ultraq.redhorizon.engine.graphics.Colour
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.resources.ResourceManager
import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.engine.scenegraph.nodes.Camera
import nz.net.ultraq.redhorizon.engine.scenegraph.nodes.GridLines
import nz.net.ultraq.redhorizon.filetypes.PngFile
import nz.net.ultraq.redhorizon.shooter.objects.Player

/**
 * Main class for the shooter game ✈️🔫
 *
 * @author Emanuel Rabina
 */
class Shooter {

	static final Dimension RENDER_RESOLUTION = new Dimension(720, 405)

	private final ResourceManager resourceManager = new ResourceManager(
		new File(System.getProperty('user.dir')),
		'nz.net.ultraq.redhorizon.filetypes')

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
			.onApplicationStart(this::applicationStart)
			.onApplicationStop(this::applicationStop)
			.start()
	}

	private void applicationStart(Application application, Scene scene) {

		camera = new Camera(RENDER_RESOLUTION)
		scene << camera

		scene << new GridLines(-RENDER_RESOLUTION.width() / 2 as int, RENDER_RESOLUTION.width() / 2 as int, 24)

		player = new Player(resourceManager.loadFile('ship_0009.png', PngFile))
		scene << player
	}

	private void applicationStop(Application application, Scene scene) {

		scene.clear()
		resourceManager.close()
	}
}
