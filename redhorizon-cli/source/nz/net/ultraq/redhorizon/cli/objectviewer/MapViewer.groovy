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
import nz.net.ultraq.redhorizon.classic.maps.MapLines
import nz.net.ultraq.redhorizon.classic.maps.MapRA
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.input.CursorPositionEvent
import nz.net.ultraq.redhorizon.engine.input.KeyControl
import nz.net.ultraq.redhorizon.engine.input.KeyEvent
import nz.net.ultraq.redhorizon.engine.input.MouseButtonEvent
import nz.net.ultraq.redhorizon.engine.input.ScrollEvent
import nz.net.ultraq.redhorizon.engine.resources.ResourceManager

import org.joml.Vector2f
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
	final boolean touchpadInput

	/**
	 * Constructor, set the map and resource manager to use for displaying a map.
	 *
	 * @param graphicsConfig
	 * @param resourceManager
	 * @param mapFile
	 * @param touchpadInput
	 */
	MapViewer(GraphicsConfiguration graphicsConfig, ResourceManager resourceManager, IniFile mapFile, boolean touchpadInput) {

		super(null, graphicsConfig)
		this.resourceManager = resourceManager
		this.mapFile = mapFile
		this.touchpadInput = touchpadInput
	}

	@Override
	protected void applicationStart() {

		super.applicationStart()
		logger.info('File details: {}', mapFile)

		// Add the map
		var map = new MapRA(resourceManager, mapFile)
		var mapInitialPosition = new Vector3f(map.initialPosition, 0)
		logger.info('Map details: {}', map)

		scene << map
		scene << new MapLines(map)
		graphicsSystem.camera.center(mapInitialPosition)

		var scaleIndex = scaleRange.findIndexOf { it == initialScale }
		var mouseMovementModifier = 1f
		var renderResolution = graphicsSystem.window.renderResolution
		var targetResolution = graphicsSystem.window.targetResolution
		mouseMovementModifier = renderResolution.width / targetResolution.width

		// Add options so it's not hard-coded to my weird inverted setup 😅
		if (touchpadInput) {
			var ctrl = false
			inputEventStream.on(KeyEvent) { event ->
				if (event.key == GLFW_KEY_LEFT_CONTROL) {
					ctrl = event.action == GLFW_PRESS || event.action == GLFW_REPEAT
				}
			}
			inputEventStream.on(ScrollEvent) { event ->

				// Zoom in/out using CTRL + scroll up/down
				if (ctrl) {
					if (event.yOffset < 0) {
						scaleIndex = Math.clamp(scaleIndex - 1, 0, scaleRange.length - 1)
					}
					else if (event.yOffset > 0) {
						scaleIndex = Math.clamp(scaleIndex + 1, 0, scaleRange.length - 1)
					}
					graphicsSystem.camera.scale(scaleRange[scaleIndex])
				}
				// Use scroll input to move around the map
				else {
					graphicsSystem.camera.translate(Math.round(3 * event.xOffset) as float, Math.round(3 * -event.yOffset) as float)
				}
			}
			inputEventStream.on(MouseButtonEvent) { event ->
				if (ctrl && event.button == GLFW_MOUSE_BUTTON_RIGHT) {
					graphicsSystem.camera.resetScale()
				}
			}
		}
		else {

			// Use click-and-drag to move around
			var cursorPosition = new Vector2f()
			var dragging = false
			inputEventStream.on(CursorPositionEvent) { event ->
				if (dragging) {
					var diffX = (cursorPosition.x - event.xPos) * mouseMovementModifier as float
					var diffY = (cursorPosition.y - event.yPos) * mouseMovementModifier as float
					graphicsSystem.camera.translate(-diffX, diffY)
				}
				cursorPosition.set(event.xPos as float, event.yPos as float)
			}
			inputEventStream.on(MouseButtonEvent) { event ->
				if (event.button == GLFW_MOUSE_BUTTON_LEFT) {
					if (event.action == GLFW_PRESS) {
						dragging = true
					}
					else if (event.action == GLFW_RELEASE) {
						dragging = false
					}
				}
			}

			// Zoom in/out using the scroll wheel
			inputEventStream.on(ScrollEvent) { event ->
				if (event.yOffset < 0) {
					scaleIndex = Math.clamp(scaleIndex - 1, 0, scaleRange.length - 1)
				}
				else if (event.yOffset > 0) {
					scaleIndex = Math.clamp(scaleIndex + 1, 0, scaleRange.length - 1)
				}
				graphicsSystem.camera.scale(scaleRange[scaleIndex])
			}
			inputEventStream.on(MouseButtonEvent) { event ->
				if (event.button == GLFW_MOUSE_BUTTON_MIDDLE) {
					graphicsSystem.camera.resetScale()
				}
			}
		}

		// Custom inputs
		inputEventStream.addControl(new KeyControl(GLFW_KEY_UP, 'Scroll up', { ->
			graphicsSystem.camera.translate(0, -TICK)
		}))
		inputEventStream.addControl(new KeyControl(GLFW_KEY_DOWN, 'Scroll down', { ->
			graphicsSystem.camera.translate(0, TICK)
		}))
		inputEventStream.addControl(new KeyControl(GLFW_KEY_LEFT, 'Scroll left', { ->
			graphicsSystem.camera.translate(TICK, 0)
		}))
		inputEventStream.addControl(new KeyControl(GLFW_KEY_RIGHT, 'Scroll right', { ->
			graphicsSystem.camera.translate(-TICK, 0)
		}))
		inputEventStream.addControl(new KeyControl(GLFW_KEY_SPACE, 'Center starting position', { ->
			graphicsSystem.camera.center(mapInitialPosition)
		}))

		logger.info('Displaying the map.  Close the window to exit.')
	}
}
