/* 
 * Copyright 2021, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import nz.net.ultraq.redhorizon.engine.Application
import nz.net.ultraq.redhorizon.engine.audio.AudioConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.WindowCreatedEvent
import nz.net.ultraq.redhorizon.engine.input.CursorPositionEvent
import nz.net.ultraq.redhorizon.engine.input.KeyEvent
import nz.net.ultraq.redhorizon.engine.input.MouseButtonEvent
import nz.net.ultraq.redhorizon.engine.input.ScrollEvent

import org.joml.Vector2f
import static org.lwjgl.glfw.GLFW.*

/**
 * Common viewer application code.
 * 
 * @author Emanuel Rabina
 */
abstract class Viewer extends Application {

	final boolean touchpadInput

	/**
	 * Constructor, set viewer-specific configuration.
	 * 
	 * @param audioConfig
	 * @param graphicsConfig
	 * @param touchpadInput
	 */
	protected Viewer(AudioConfiguration audioConfig, GraphicsConfiguration graphicsConfig, boolean touchpadInput) {

		super(null, audioConfig, graphicsConfig)
		this.touchpadInput = touchpadInput
	}

	@Override
	protected void applicationStart() {

		def mouseMovementModifier = 1f
		graphicsEngine.on(WindowCreatedEvent) { event ->
			def renderResolution = graphicsEngine.graphicsContext.renderResolution
			def targetResolution = graphicsEngine.graphicsContext.targetResolution
			mouseMovementModifier = renderResolution.width / targetResolution.width
		}

		def scaleTicks = (1.0..4.0).by(0.1) as float[]
		def scaleIndex = scaleTicks.findIndexOf { it == 2.0 }

		// Key event handler
		inputEventStream.on(KeyEvent) { event ->
			if (event.action == GLFW_PRESS || event.action == GLFW_REPEAT) {
				switch (event.key) {
					case GLFW_KEY_ESCAPE:
						stop()
						break
				}
			}
		}

		// Add options so it's not hard-coded to my weird inverted setup 😅
		if (touchpadInput) {
			def ctrl = false
			inputEventStream.on(KeyEvent) { event ->
				if (event.key == GLFW_KEY_LEFT_CONTROL) {
					ctrl = event.action == GLFW_PRESS || event.action == GLFW_REPEAT
				}
			}
			inputEventStream.on(ScrollEvent) { event ->

				// Zoom in/out using CTRL + scroll up/down
				if (ctrl) {
					if (event.yOffset < 0) {
						scaleIndex = Math.clamp(scaleIndex - 1, 0, scaleTicks.length - 1)
					}
					else if (event.yOffset > 0) {
						scaleIndex = Math.clamp(scaleIndex + 1, 0, scaleTicks.length - 1)
					}
					graphicsEngine.camera.scale(scaleTicks[scaleIndex])
				}
				// Use scroll input to move around the map
				else {
					graphicsEngine.camera.translate(3 * event.xOffset as float, 3 * -event.yOffset as float)
				}
			}
			inputEventStream.on(MouseButtonEvent) { event ->
				if (ctrl && event.button == GLFW_MOUSE_BUTTON_RIGHT) {
					graphicsEngine.camera.resetScale()
				}
			}
		}
		else {

			// Use click-and-drag to move around
			def cursorPosition = new Vector2f()
			def dragging = false
			inputEventStream.on(CursorPositionEvent) { event ->
				if (dragging) {
					def diffX = (cursorPosition.x - event.xPos) * mouseMovementModifier as float
					def diffY = (cursorPosition.y - event.yPos) * mouseMovementModifier as float
					graphicsEngine.camera.translate(-diffX, diffY)
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
					scaleIndex = Math.clamp(scaleIndex - 1, 0, scaleTicks.length - 1)
				}
				else if (event.yOffset > 0) {
					scaleIndex = Math.clamp(scaleIndex + 1, 0, scaleTicks.length - 1)
				}
				graphicsEngine.camera.scale(scaleTicks[scaleIndex])
			}
			inputEventStream.on(MouseButtonEvent) { event ->
				if (event.button == GLFW_MOUSE_BUTTON_MIDDLE) {
					graphicsEngine.camera.resetScale()
				}
			}
		}
	}
}
