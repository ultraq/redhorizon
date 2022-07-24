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

import nz.net.ultraq.redhorizon.classic.filetypes.ini.IniFile
import nz.net.ultraq.redhorizon.cli.objectviewer.maps.MapLines
import nz.net.ultraq.redhorizon.cli.objectviewer.maps.MapRA
import nz.net.ultraq.redhorizon.engine.EngineLoopStartEvent
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.WindowCreatedEvent
import nz.net.ultraq.redhorizon.engine.input.KeyEvent
import nz.net.ultraq.redhorizon.engine.resources.ResourceManager

import org.joml.Vector3f
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.*

/**
 * A map viewer for testing map building.
 * 
 * @author Emanuel Rabina
 */
class MapViewer extends Viewer {

	private static final Logger logger = LoggerFactory.getLogger(MapViewer)
	private static final int TICK = 48

	final float initialScale = 1.0f
	final float[] scaleRange = (1.0..2.0).by(0.1)

	final ResourceManager resourceManager
	final IniFile mapFile

	/**
	 * Constructor, set the map and resource manager to use for displaying a map.
	 * 
	 * @param graphicsConfig
	 * @param resourceManager
	 * @param mapFile
	 * @param touchpadInput
	 */
	MapViewer(GraphicsConfiguration graphicsConfig, ResourceManager resourceManager, IniFile mapFile, boolean touchpadInput) {

		super(null, graphicsConfig, touchpadInput)
		this.resourceManager = resourceManager
		this.mapFile = mapFile
	}

	@Override
	protected void applicationStart() {

		super.applicationStart()
		logger.info('File details: {}', mapFile)

		// Add the map
		MapRA map
		Vector3f mapInitialPosition
		graphicsEngine.on(WindowCreatedEvent) { event ->
			map = new MapRA(resourceManager, mapFile)
			mapInitialPosition = new Vector3f(map.initialPosition, 0)
			logger.info('Map details: {}', map)

			graphicsEngine.on(EngineLoopStartEvent) { engineLoopStartEvent ->
				graphicsEngine.camera.center(mapInitialPosition)
				scene << map
				scene << new MapLines(map)
			}
		}

		logger.info('Displaying the image in another window.  Close the window to exit.')

		// Custom inputs
		inputEventStream.on(KeyEvent) { event ->
			if (event.action == GLFW_PRESS || event.action == GLFW_REPEAT) {
				switch (event.key) {
					case GLFW_KEY_UP:
						graphicsEngine.camera.translate(0, -TICK)
						break
					case GLFW_KEY_DOWN:
						graphicsEngine.camera.translate(0, TICK)
						break
					case GLFW_KEY_LEFT:
						graphicsEngine.camera.translate(TICK, 0)
						break
					case GLFW_KEY_RIGHT:
						graphicsEngine.camera.translate(-TICK, 0)
						break
					case GLFW_KEY_SPACE:
						graphicsEngine.camera.center(mapInitialPosition)
						break
				}
			}
		}
	}
}
