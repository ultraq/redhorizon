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
import nz.net.ultraq.redhorizon.utilities.objectviewer.maps.Map

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE

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
				def map = new Map(mapFile)
				logger.info('Map details: {}', map)
				graphicsEngine.addSceneElement(map)

				logger.info('Displaying the image in another window.  Close the window to exit.')

				// Key event handler
				graphicsEngine.on(KeyEvent) { event ->
					switch (event.key) {
						case GLFW_KEY_ESCAPE:
							graphicsEngine.stop()
							break
					}
				}
			}
		}
	}
}