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
import nz.net.ultraq.redhorizon.engine.scenegraph.nodes.Camera
import nz.net.ultraq.redhorizon.engine.scenegraph.scripting.Script
import nz.net.ultraq.redhorizon.events.RemoveEventFunction
import nz.net.ultraq.redhorizon.explorer.animation.EasingFunctions
import nz.net.ultraq.redhorizon.explorer.animation.Transition

import org.joml.Vector2f
import org.joml.Vector3f
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

	final Camera camera
	boolean touchpadInput

	private final List<RemoveEventFunction> removeEventFunctions = []
	private final List<RemoveControlFunction> removeControlFunctions = []
	private InputEventStream inputEventStream
	private Window window
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

		// Use touchpad to move around
		if (touchpadInput) {
			var ctrl = false
			removeEventFunctions << inputEventStream.on(KeyEvent) { event ->
				if (event.key == GLFW_KEY_LEFT_CONTROL) {
					ctrl = event.action == GLFW_PRESS || event.action == GLFW_REPEAT
				}
			}
			removeEventFunctions << inputEventStream.on(ScrollEvent) { event ->
				if (gameWindow ? gameWindow.hovered : true) {

					// Zoom in/out using CTRL + scroll up/down
					if (ctrl) {
						float scaleFactor = camera.scale.x
						if (event.yOffset < 0) {
							scaleFactor -= 0.1
						}
						else if (event.yOffset > 0) {
							scaleFactor += 0.1
						}
						scaleFactor = Math.clamp(scaleFactor, 1f, 2f)
						camera.setScaleXY(scaleFactor)
					}
					// Use scroll input to move around the map
					else {
						camera.transform.translate(Math.round(3 * event.xOffset) as float, Math.round(3 * -event.yOffset) as float)
					}
				}
			}
			removeControlFunctions << inputEventStream.addControl(
				new MouseControl(GLFW_MOD_CONTROL, GLFW_MOUSE_BUTTON_RIGHT, 'Reset camera', { ->
					if (gameWindow ? gameWindow.hovered : true) {
						camera.reset()
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
					camera.transform.translate(-diffX, diffY)
				}
				cursorPosition.set(event.xPos as float, event.yPos as float)
			}
			removeEventFunctions << inputEventStream.on(MouseButtonEvent) { event ->
				if (event.button == GLFW_MOUSE_BUTTON_LEFT) {
					if (event.action == GLFW_PRESS) {
						if (gameWindow ? gameWindow.hovered : true) {
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
				if (gameWindow ? gameWindow.hovered : true) {
					float scaleFactor = camera.scale.x
					if (event.yOffset < 0) {
						scaleFactor -= 0.1
					}
					else if (event.yOffset > 0) {
						scaleFactor += 0.1
					}
					scaleFactor = Math.clamp(scaleFactor, 1f, 2f)
					camera.setScaleXY(scaleFactor)
				}
			}
			removeControlFunctions << inputEventStream.addControl(
				new MouseControl(GLFW_MOUSE_BUTTON_RIGHT, 'Reset camera', { ->
					if (gameWindow ? gameWindow.hovered : true) {
						camera.reset()
					}
				})
			)
		}

		// Custom inputs
		removeControlFunctions << inputEventStream.addControl(new KeyControl(GLFW_KEY_W, 'Scroll up', { ->
			camera.transform.translate(0, -TICK)
		}))
		removeControlFunctions << inputEventStream.addControl(new KeyControl(GLFW_KEY_S, 'Scroll down', { ->
			camera.transform.translate(0, TICK)
		}))
		removeControlFunctions << inputEventStream.addControl(new KeyControl(GLFW_KEY_A, 'Scroll left', { ->
			camera.transform.translate(TICK, 0)
		}))
		removeControlFunctions << inputEventStream.addControl(new KeyControl(GLFW_KEY_D, 'Scroll right', { ->
			camera.transform.translate(-TICK, 0)
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
			gameWindow = scene.gameWindow
			addControls()
		}
	}

	@Override
	CompletableFuture<Void> onSceneRemoved(Scene scene) {

		return CompletableFuture.runAsync { ->
			clearControls()
		}
	}

	/**
	 * Reset controls to optimize touchpad or mouse usage.
	 */
	void setTouchpadInput(boolean touchpadInput) {

		this.touchpadInput = touchpadInput
		clearControls()
		addControls()
	}

	/**
	 * Recenter the camera on the map initial position.
	 */
	CompletableFuture<Void> viewInitialPosition() {

		var startPosition = new Vector3f(camera.position)
		var endPosition = new Vector3f(initialPosition, 0)
		var nextPosition = new Vector3f()

		return new Transition(EasingFunctions::easeOutCubic, 800, { float delta ->
			camera.position = startPosition.lerp(endPosition, delta, nextPosition)
		}).start()
	}
}
