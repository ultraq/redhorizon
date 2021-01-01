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

package nz.net.ultraq.redhorizon.utilities.objectviewer

import nz.net.ultraq.redhorizon.classic.filetypes.ini.IniFile
import nz.net.ultraq.redhorizon.engine.KeyEvent
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.WithGraphicsEngine
import nz.net.ultraq.redhorizon.utilities.objectviewer.maps.MapRA

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UP
import static org.lwjgl.glfw.GLFW.GLFW_PRESS
import static org.lwjgl.glfw.GLFW.GLFW_REPEAT

import groovy.transform.TupleConstructor
import java.util.concurrent.Executors

/**
 * A map viewer for testing map building.
 * 
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class MapViewer implements WithGraphicsEngine {

	private static final Logger logger = LoggerFactory.getLogger(MapViewer)

	final IniFile mapFile

	/**
	 * Display the map.
	 */
	void view() {

		logger.info('File details: {}', mapFile)

		def config = new GraphicsConfiguration()

		Executors.newCachedThreadPool().executeAndShutdown { executorService ->
			withGraphicsEngine(executorService, config) { graphicsEngine ->

				// Add the map
				def map = new MapRA(mapFile)
				logger.info('Map details: {}', map)
				graphicsEngine.addSceneElement(map)
				graphicsEngine.camera.position.set(map.initialPosition, 0)

				logger.info('Displaying the image in another window.  Close the window to exit.')

				// Key event handler
				graphicsEngine.on(KeyEvent) { event ->
					if (event.action == GLFW_PRESS || event.action == GLFW_REPEAT) {
						switch (event.key) {
							case GLFW_KEY_DOWN:
								graphicsEngine.camera.moveDown()
								break
							case GLFW_KEY_LEFT:
								graphicsEngine.camera.moveLeft()
								break
							case GLFW_KEY_RIGHT:
								graphicsEngine.camera.moveRight()
								break
							case GLFW_KEY_UP:
								graphicsEngine.camera.moveUp()
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
