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
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.WindowCreatedEvent
import nz.net.ultraq.redhorizon.engine.input.KeyEvent
import nz.net.ultraq.redhorizon.resources.ResourceManager
import nz.net.ultraq.redhorizon.scenegraph.Scene

import org.joml.Vector3f
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.*

import groovy.transform.TupleConstructor
import java.util.concurrent.Executors

/**
 * A map viewer for testing map building.
 * 
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class MapViewer extends Viewer {

	private static final Logger logger = LoggerFactory.getLogger(MapViewer)
	private static final int TICK = 48

	final ResourceManager resourceManager
	final IniFile mapFile
	final GraphicsConfiguration graphicsConfig
	final boolean touchpadInput

	/**
	 * Display the map.
	 */
	void view() {

		logger.info('File details: {}', mapFile)

		def scene = new Scene()

		Executors.newCachedThreadPool().executeAndShutdown { executorService ->
			useGraphicsEngine(executorService, graphicsConfig) { graphicsEngine ->
				graphicsEngine.scene = scene

				// Add the map
				MapRA map
				Vector3f mapInitialPosition
				graphicsEngine.on(WindowCreatedEvent) { event ->
					map = new MapRA(resourceManager, mapFile)
					mapInitialPosition = new Vector3f(map.initialPosition, 0)
					logger.info('Map details: {}', map)
					scene << map
					graphicsEngine.camera.center(mapInitialPosition)

					scene << new MapLines(map)
				}

				logger.info('Displaying the image in another window.  Close the window to exit.')

				applyViewerInputs(graphicsEngine, touchpadInput)

				// Custom inputs
				graphicsEngine.on(KeyEvent) { event ->
					if (event.action == GLFW_PRESS || event.action == GLFW_REPEAT) {
						switch (event.key) {
							// Add options so it's not hard-coded to my weird inverted setup 😅
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
	}
}
