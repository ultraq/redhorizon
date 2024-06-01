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

import nz.net.ultraq.redhorizon.classic.maps.Map
import nz.net.ultraq.redhorizon.engine.graphics.Camera
import nz.net.ultraq.redhorizon.engine.graphics.Window
import nz.net.ultraq.redhorizon.engine.graphics.imgui.ImGuiLayer.GameWindow
import nz.net.ultraq.redhorizon.engine.input.CursorPositionEvent
import nz.net.ultraq.redhorizon.engine.input.InputEventStream
import nz.net.ultraq.redhorizon.engine.input.KeyControl
import nz.net.ultraq.redhorizon.engine.input.KeyEvent
import nz.net.ultraq.redhorizon.engine.input.MouseButtonEvent
import nz.net.ultraq.redhorizon.engine.input.MouseControl
import nz.net.ultraq.redhorizon.engine.input.RemoveControlFunction
import nz.net.ultraq.redhorizon.engine.input.ScrollEvent
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.engine.scenegraph.scripting.Script
import nz.net.ultraq.redhorizon.events.RemoveEventFunction

import org.joml.Vector2f
import static org.lwjgl.glfw.GLFW.*

import groovy.transform.TupleConstructor
import java.util.concurrent.CompletableFuture

/**
 * Controls for viewing a map in the explorer.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class MapViewerScript extends Script<Map> {

	private static final int TICK = 48

	boolean touchpadInput

	private final float initialScale = 1.0f
	private final float[] scaleRange = (1.0..2.0).by(0.1)
	private final List<RemoveEventFunction> removeEventFunctions = []
	private final List<RemoveControlFunction> removeControlFunctions = []

	private InputEventStream inputEventStream
	private Window window
	private Camera camera
	private GameWindow gameWindow

	@Delegate
	Map applyDelegate() {
		return scriptable
	}

	/**
	 * Apply all of the controls based on whether we are using touchpad input or
	 * not.
	 */
	private void addControls() {

		var scaleIndex = scaleRange.findIndexOf { it == initialScale }
		var renderResolution = window.renderResolution
		var targetResolution = window.targetResolution

		// Use touchpad to move around
		if (touchpadInput) {
			var ctrl = false
			removeEventFunctions << inputEventStream.on(KeyEvent) { event ->
				if (event.key == GLFW_KEY_LEFT_CONTROL) {
					ctrl = event.action == GLFW_PRESS || event.action == GLFW_REPEAT
				}
			}
			removeEventFunctions << inputEventStream.on(ScrollEvent) { event ->
				if (gameWindow.hovered) {

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
			}
			removeControlFunctions << inputEventStream.addControl(
				new MouseControl(GLFW_MOD_CONTROL, GLFW_MOUSE_BUTTON_RIGHT, 'Reset scale', { ->
					if (gameWindow.hovered) {
						camera.resetScale()
					}
				})
			)
		}
		// Use click-and-drag to move around
		else {
			var cursorPosition = new Vector2f()
			var dragging = false
			removeEventFunctions << inputEventStream.on(CursorPositionEvent) { event ->
				if (dragging) {
					var diffX = (cursorPosition.x - event.xPos) as float
					var diffY = (cursorPosition.y - event.yPos) as float
					camera.translate(-diffX, diffY)
				}
				cursorPosition.set(event.xPos as float, event.yPos as float)
			}
			removeEventFunctions << inputEventStream.on(MouseButtonEvent) { event ->
				if (event.button == GLFW_MOUSE_BUTTON_LEFT) {
					if (event.action == GLFW_PRESS) {
						if (gameWindow.hovered) {
							dragging = true
						}
					}
					else if (event.action == GLFW_RELEASE) {
						dragging = false
					}
				}
			}

			// Zoom in/out using the scroll wheel
			removeEventFunctions << inputEventStream.on(ScrollEvent) { event ->
				if (gameWindow.hovered) {
					if (event.yOffset < 0) {
						scaleIndex = Math.clamp(scaleIndex - 1, 0, scaleRange.length - 1)
					}
					else if (event.yOffset > 0) {
						scaleIndex = Math.clamp(scaleIndex + 1, 0, scaleRange.length - 1)
					}
					camera.scale(scaleRange[scaleIndex])
				}
			}
			removeControlFunctions << inputEventStream.addControl(
				new MouseControl(GLFW_MOUSE_BUTTON_RIGHT, 'Reset scale', { ->
					if (gameWindow.hovered) {
						camera.resetScale()
					}
				})
			)
		}

		// Custom inputs
		removeControlFunctions << inputEventStream.addControl(new KeyControl(GLFW_KEY_W, 'Scroll up', { ->
			camera.translate(0, -TICK)
		}))
		removeControlFunctions << inputEventStream.addControl(new KeyControl(GLFW_KEY_S, 'Scroll down', { ->
			camera.translate(0, TICK)
		}))
		removeControlFunctions << inputEventStream.addControl(new KeyControl(GLFW_KEY_A, 'Scroll left', { ->
			camera.translate(TICK, 0)
		}))
		removeControlFunctions << inputEventStream.addControl(new KeyControl(GLFW_KEY_D, 'Scroll right', { ->
			camera.translate(-TICK, 0)
		}))
		removeControlFunctions << inputEventStream.addControl(new KeyControl(GLFW_KEY_SPACE, 'Reset camera position', { ->
			viewInitialPosition()
		}))
	}

	/**
	 * Clear all registered controls.
	 */
	private void clearControls() {

		removeEventFunctions*.remove()
		removeControlFunctions*.remove()
	}

	@Override
	CompletableFuture<Void> onSceneAdded(Scene scene) {

		return CompletableFuture.runAsync { ->
			inputEventStream = scene.inputEventStream
			window = scene.window
			camera = scene.camera
			gameWindow = scene.gameWindow
			addControls()
			viewInitialPosition()
		}
	}

	@Override
	CompletableFuture<Void> onSceneRemoved(Scene scene) {

		return CompletableFuture.runAsync { ->
			clearControls()
			camera.resetScale()
		}
	}

	void setTouchpadInput(boolean touchpadInput) {

		this.touchpadInput = touchpadInput
		clearControls()
		addControls()
	}

	private void viewInitialPosition() {

		camera.center(initialPosition.x(), initialPosition.y())
	}
}
