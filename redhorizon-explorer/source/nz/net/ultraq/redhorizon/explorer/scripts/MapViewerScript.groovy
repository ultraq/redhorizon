/*
 * Copyright 2024, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.explorer.scripts

import nz.net.ultraq.redhorizon.engine.input.CursorPositionEvent
import nz.net.ultraq.redhorizon.engine.input.KeyControl
import nz.net.ultraq.redhorizon.engine.input.KeyEvent
import nz.net.ultraq.redhorizon.engine.input.MouseButtonEvent
import nz.net.ultraq.redhorizon.engine.input.MouseControl
import nz.net.ultraq.redhorizon.engine.input.RemoveControlFunction
import nz.net.ultraq.redhorizon.engine.input.ScrollEvent
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.engine.scenegraph.scripting.Script
import nz.net.ultraq.redhorizon.events.DeregisterEventFunction
import nz.net.ultraq.redhorizon.explorer.objects.Map

import org.joml.Vector2f
import static org.lwjgl.glfw.GLFW.*

import groovy.transform.TupleConstructor

/**
 * Controls for viewing a map in the explorer.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class MapViewerScript extends Script<Map> {

	private static final int TICK = 48

	private final float initialScale = 1.0f
	private final float[] scaleRange = (1.0..2.0).by(0.1)
	private final List<DeregisterEventFunction> deregisterEventFunctions = []
	private final List<RemoveControlFunction> removeControlFunctions = []

	final boolean touchpadInput

	@Delegate
	Map applyDelegate() {
		return scriptable
	}

	@Override
	void onSceneAdded(Scene scene) {

		var inputEventStream = scene.inputEventStream
		var window = scene.window
		var camera = scene.camera

		var scaleIndex = scaleRange.findIndexOf { it == initialScale }
		var mouseMovementModifier = 1f
		var renderResolution = window.renderResolution
		var targetResolution = window.targetResolution
		mouseMovementModifier = renderResolution.width / targetResolution.width

		// Add options so it's not hard-coded to my weird inverted setup 😅
		if (touchpadInput) {
			var ctrl = false
			deregisterEventFunctions << inputEventStream.on(KeyEvent) { event ->
				if (event.key == GLFW_KEY_LEFT_CONTROL) {
					ctrl = event.action == GLFW_PRESS || event.action == GLFW_REPEAT
				}
			}
			deregisterEventFunctions << inputEventStream.on(ScrollEvent) { event ->

				// Zoom in/out using CTRL + scroll up/down
				if (ctrl) {
					if (event.yOffset < 0) {
						scaleIndex = Math.clamp(scaleIndex - 1, 0, scaleRange.length - 1)
					}
					else if (event.yOffset > 0) {
						scaleIndex = Math.clamp(scaleIndex + 1, 0, scaleRange.length - 1)
					}
					camera.scale(scaleRange[scaleIndex])
				}
				// Use scroll input to move around the map
				else {
					camera.translate(Math.round(3 * event.xOffset) as float, Math.round(3 * -event.yOffset) as float)
				}
			}
			removeControlFunctions << inputEventStream.addControl(
				new MouseControl(GLFW_MOD_CONTROL, GLFW_MOUSE_BUTTON_RIGHT, 'Reset scale', { ->
					camera.resetScale()
				})
			)
		}
		else {

			// Use click-and-drag to move around
			var cursorPosition = new Vector2f()
			var dragging = false
			deregisterEventFunctions << inputEventStream.on(CursorPositionEvent) { event ->
				if (dragging) {
					var diffX = (cursorPosition.x - event.xPos) * mouseMovementModifier as float
					var diffY = (cursorPosition.y - event.yPos) * mouseMovementModifier as float
					camera.translate(-diffX, diffY)
				}
				cursorPosition.set(event.xPos as float, event.yPos as float)
			}
			deregisterEventFunctions << inputEventStream.on(MouseButtonEvent) { event ->
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
			deregisterEventFunctions << inputEventStream.on(ScrollEvent) { event ->
				if (event.yOffset < 0) {
					scaleIndex = Math.clamp(scaleIndex - 1, 0, scaleRange.length - 1)
				}
				else if (event.yOffset > 0) {
					scaleIndex = Math.clamp(scaleIndex + 1, 0, scaleRange.length - 1)
				}
				camera.scale(scaleRange[scaleIndex])
			}
			removeControlFunctions << inputEventStream.addControl(
				new MouseControl(GLFW_MOD_CONTROL, GLFW_MOUSE_BUTTON_MIDDLE, 'Reset scale', { ->
					camera.resetScale()
				})
			)
		}

		// Custom inputs
		removeControlFunctions << inputEventStream.addControl(new KeyControl(GLFW_KEY_UP, 'Scroll up', { ->
			camera.translate(0, -TICK)
		}))
		removeControlFunctions << inputEventStream.addControl(new KeyControl(GLFW_KEY_DOWN, 'Scroll down', { ->
			camera.translate(0, TICK)
		}))
		removeControlFunctions << inputEventStream.addControl(new KeyControl(GLFW_KEY_LEFT, 'Scroll left', { ->
			camera.translate(TICK, 0)
		}))
		removeControlFunctions << inputEventStream.addControl(new KeyControl(GLFW_KEY_RIGHT, 'Scroll right', { ->
			camera.translate(-TICK, 0)
		}))
		removeControlFunctions << inputEventStream.addControl(new KeyControl(GLFW_KEY_SPACE, 'Reset camera position', { ->
			camera.center(initialPosition.x(), initialPosition.y())
		}))
	}

	@Override
	void onSceneRemoved(Scene scene) {

		deregisterEventFunctions*.deregister()
		removeControlFunctions*.remove()
	}
}
