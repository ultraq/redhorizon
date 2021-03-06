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

import nz.net.ultraq.redhorizon.Application
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsEngine
import nz.net.ultraq.redhorizon.engine.input.CursorPositionEvent
import nz.net.ultraq.redhorizon.engine.input.InputEngine
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

	/**
	 * Listen on the input engine to apply the same controls in all viewer
	 * applications.
	 * 
	 * @param inputEngine
	 * @param graphicsEngine
	 * @param touchpadInput
	 */
	protected static void applyViewerInputs(InputEngine inputEngine, GraphicsEngine graphicsEngine, boolean touchpadInput) {

		// Key event handler
		inputEngine.on(KeyEvent) { event ->
			if (event.action == GLFW_PRESS || event.action == GLFW_REPEAT) {
				switch (event.key) {
				case GLFW_KEY_ESCAPE:
					graphicsEngine.stop()
					inputEngine.stop()
					break
				}
			}
		}

		if (touchpadInput) {
			def ctrl = false
			inputEngine.on(KeyEvent) { event ->
				if (event.key == GLFW_KEY_LEFT_CONTROL) {
					ctrl = event.action == GLFW_PRESS || event.action == GLFW_REPEAT
				}
			}
			inputEngine.on(ScrollEvent) { event ->

				// Zoom in/out using CTRL + scroll up/down
				if (ctrl) {
					if (event.yOffset < 0) {
						graphicsEngine.camera.scale(0.95)
					}
					else if (event.yOffset > 0) {
						graphicsEngine.camera.scale(1.05)
					}
				}
				// Use scroll input to move around the map
				else {
					graphicsEngine.camera.translate(3 * event.xOffset as float, 3 * -event.yOffset as float)
				}
			}
			inputEngine.on(MouseButtonEvent) { event ->
				if (ctrl && event.button == GLFW_MOUSE_BUTTON_RIGHT) {
					graphicsEngine.camera.resetScale()
				}
			}
		}
		else {

			// Use click-and-drag to move around
			def cursorPosition = new Vector2f()
			def dragging = false
			inputEngine.on(CursorPositionEvent) { event ->
				if (dragging) {
					def diffX = cursorPosition.x - event.xPos as float
					def diffY = cursorPosition.y - event.yPos as float
					graphicsEngine.camera.translate(-diffX, diffY)
				}
				cursorPosition.set(event.xPos as float, event.yPos as float)
			}
			inputEngine.on(MouseButtonEvent) { event ->
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
			inputEngine.on(ScrollEvent) { event ->
				if (event.yOffset < 0) {
					graphicsEngine.camera.scale(0.95)
				}
				else if (event.yOffset > 0) {
					graphicsEngine.camera.scale(1.05)
				}
			}
			inputEngine.on(MouseButtonEvent) { event ->
				if (event.button == GLFW_MOUSE_BUTTON_MIDDLE) {
					graphicsEngine.camera.resetScale()
				}
			}
		}
	}
}
