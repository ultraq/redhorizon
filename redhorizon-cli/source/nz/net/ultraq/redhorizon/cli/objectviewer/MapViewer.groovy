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
import nz.net.ultraq.redhorizon.cli.objectviewer.maps.MapLines
import nz.net.ultraq.redhorizon.cli.objectviewer.maps.MapRA
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.input.KeyControl
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
		var map = new MapRA(resourceManager, mapFile)
		var mapInitialPosition = new Vector3f(map.initialPosition, 0)
		logger.info('Map details: {}', map)

		graphicsEngine.camera.center(mapInitialPosition)
		scene << map
		scene << new MapLines(map)

		logger.info('Displaying the map.  Close the window to exit.')

		// Custom inputs
		inputEventStream.addControl(new KeyControl(GLFW_KEY_UP, 'Scroll up', { ->
			graphicsEngine.camera.translate(0, -TICK)
		}))
		inputEventStream.addControl(new KeyControl(GLFW_KEY_DOWN, 'Scroll down', { ->
			graphicsEngine.camera.translate(0, TICK)
		}))
		inputEventStream.addControl(new KeyControl(GLFW_KEY_LEFT, 'Scroll left', { ->
			graphicsEngine.camera.translate(TICK, 0)
		}))
		inputEventStream.addControl(new KeyControl(GLFW_KEY_RIGHT, 'Scroll right', { ->
			graphicsEngine.camera.translate(-TICK, 0)
		}))
		inputEventStream.addControl(new KeyControl(GLFW_KEY_SPACE, 'Center starting position', { ->
			graphicsEngine.camera.center(mapInitialPosition)
		}))
	}
}
