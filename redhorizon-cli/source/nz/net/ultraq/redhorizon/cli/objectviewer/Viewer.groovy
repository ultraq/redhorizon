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
	 * Listen on the graphics engine to apply the same controls in all viewer
	 * applications.
	 * 
	 * @param graphicsEngine
	 */
	protected static void applyViewerInputs(GraphicsEngine graphicsEngine) {

		// Key event handler
		graphicsEngine.on(KeyEvent) { event ->
			if (event.action == GLFW_PRESS || event.action == GLFW_REPEAT) {
				switch (event.key) {
				case GLFW_KEY_ESCAPE:
					graphicsEngine.stop()
					break
				}
			}
		}

		// Use click-and-drag to move around
		def cursorPosition = new Vector2f()
		def dragging = false
		graphicsEngine.on(CursorPositionEvent) { event ->
			if (dragging) {
				def diffX = cursorPosition.x - event.xPos as float
				def diffY = cursorPosition.y - event.yPos as float
				graphicsEngine.camera.translate(-diffX, diffY)
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

		// Zoom in/out using the scroll wheel
		graphicsEngine.on(ScrollEvent) { event ->
			if (event.yOffset < 0) {
				graphicsEngine.camera.scale(0.95)
			}
			else if (event.yOffset > 0) {
				graphicsEngine.camera.scale(1.05)
			}
		}
		graphicsEngine.on(MouseButtonEvent) { event ->
			if (event.button == GLFW_MOUSE_BUTTON_MIDDLE) {
				graphicsEngine.camera.resetScale()
			}
		}
	}
}
