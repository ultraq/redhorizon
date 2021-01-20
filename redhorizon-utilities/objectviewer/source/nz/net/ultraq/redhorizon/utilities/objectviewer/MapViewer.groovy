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
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.WindowCreatedEvent
import nz.net.ultraq.redhorizon.engine.graphics.WithGraphicsEngine
import nz.net.ultraq.redhorizon.engine.input.CursorPositionEvent
import nz.net.ultraq.redhorizon.engine.input.KeyEvent
import nz.net.ultraq.redhorizon.engine.input.MouseButtonEvent
import nz.net.ultraq.redhorizon.engine.input.ScrollEvent
import nz.net.ultraq.redhorizon.resources.ResourceManager
import nz.net.ultraq.redhorizon.utilities.objectviewer.maps.MapRA

import org.joml.Vector2f
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UP
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT
import static org.lwjgl.glfw.GLFW.GLFW_PRESS
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE
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
	private static final int TICK = 48

	final ResourceManager resourceManager
	final IniFile mapFile
	final boolean fullScreen
	final boolean modernRenderer

	/**
	 * Display the map.
	 */
	void view() {

		logger.info('File details: {}', mapFile)

		def config = new GraphicsConfiguration(
			fullScreen: fullScreen,
			modernRenderer: modernRenderer
		)

		Executors.newCachedThreadPool().executeAndShutdown { executorService ->
			withGraphicsEngine(executorService, config) { graphicsEngine ->

				// Add the map
				MapRA map
				graphicsEngine.on(WindowCreatedEvent) { event ->
					map = new MapRA(resourceManager, mapFile)
					logger.info('Map details: {}', map)
					graphicsEngine.addSceneElement(map)
					graphicsEngine.camera.position.set(map.initialPosition, 0)
				}

				logger.info('Displaying the image in another window.  Close the window to exit.')

				// Key event handler
				graphicsEngine.on(KeyEvent) { event ->
					if (event.action == GLFW_PRESS || event.action == GLFW_REPEAT) {
						switch (event.key) {
							case GLFW_KEY_DOWN:
								graphicsEngine.camera.position.add(0, -TICK, 0)
								break
							case GLFW_KEY_LEFT:
								graphicsEngine.camera.position.add(-TICK, 0, 0)
								break
							case GLFW_KEY_RIGHT:
								graphicsEngine.camera.position.add(TICK, 0, 0)
								break
							case GLFW_KEY_UP:
								graphicsEngine.camera.position.add(0, TICK, 0)
								break
							case GLFW_KEY_SPACE:
								graphicsEngine.camera.position.set(map.initialPosition, 0)
								break
							case GLFW_KEY_ESCAPE:
								graphicsEngine.stop()
								break
						}
					}
				}

				// Use scroll input or click-and-drag to move around the map
				graphicsEngine.on(ScrollEvent) { event ->
					graphicsEngine.camera.position.add(3 * -event.xOffset as float, 3 * event.yOffset as float, 0)
				}
				def cursorPosition = new Vector2f()
				def dragging = false
				graphicsEngine.on(CursorPositionEvent) { event ->
					if (dragging) {
						def diffX = cursorPosition.x - event.xPos as float
						def diffY = cursorPosition.y - event.yPos as float
						graphicsEngine.camera.position.add(diffX, -diffY, 0)
					}
					cursorPosition.set(event.xPos as float, event.yPos as float)
				}
				graphicsEngine.on(MouseButtonEvent) { event ->
					if (event.button == GLFW_MOUSE_BUTTON_LEFT) {
						if (event.action == GLFW_PRESS) {
							dragging = true
						}
						else if (event.action == GLFW_RELEASE) {
							dragging = false
						}
					}
				}
			}
		}
	}
}
