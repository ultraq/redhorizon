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
import nz.net.ultraq.redhorizon.engine.graphics.Colour
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
import nz.net.ultraq.redhorizon.engine.scenegraph.nodes.Outline
import nz.net.ultraq.redhorizon.engine.scenegraph.scripting.Script
import nz.net.ultraq.redhorizon.events.RemoveEventFunction
import nz.net.ultraq.redhorizon.explorer.NodeList
import nz.net.ultraq.redhorizon.explorer.NodeSelectedEvent
import nz.net.ultraq.redhorizon.explorer.animation.EasingFunctions
import nz.net.ultraq.redhorizon.explorer.animation.Transition

import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.primitives.Rectanglef
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
	final NodeList nodeList
	boolean touchpadInput

	private final List<RemoveEventFunction> removeEventFunctions = []
	private final List<RemoveControlFunction> removeControlFunctions = []
	private InputEventStream inputEventStream
	private Window window
	private GameWindow gameWindow
	private Outline outline

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
						float scaleFactor = camera.scale.x()
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
						camera.transform { ->
							translate(Math.round(3 * event.xOffset) as float, Math.round(3 * -event.yOffset) as float)
						}
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
				var adjustedPosX = event.xPos / window.renderToWindowScale as float
				var adjustedPosY = event.yPos / window.renderToWindowScale as float
				if (dragging) {
					var scale = camera.scale
					var diffX = (cursorPosition.x - adjustedPosX) / scale.x() as float
					var diffY = (cursorPosition.y - adjustedPosY) / scale.y() as float
					camera.transform { ->
						translate(-diffX, diffY)
					}
				}
				cursorPosition.set(adjustedPosX, adjustedPosY)
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
					float scaleFactor = camera.scale.x()
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

		nodeList.on(NodeSelectedEvent) { event ->
			var node = event.node
			outline.updatePoints(node.globalBounds as Vector2f[])
			moveCameraTo(
				new Vector3f(
					(node.globalBounds.minX + node.globalBounds.maxX) / 2 as float,
					(node.globalBounds.minY + node.globalBounds.maxY) / 2 as float,
					0
				)
			)
		}

		// Custom inputs
		removeControlFunctions << inputEventStream.addControl(new KeyControl(GLFW_KEY_W, 'Scroll up', { ->
			camera.transform { ->
				translate(0, -TICK)
			}
		}))
		removeControlFunctions << inputEventStream.addControl(new KeyControl(GLFW_KEY_S, 'Scroll down', { ->
			camera.transform { ->
				translate(0, TICK)
			}
		}))
		removeControlFunctions << inputEventStream.addControl(new KeyControl(GLFW_KEY_A, 'Scroll left', { ->
			camera.transform { ->
				translate(TICK, 0)
			}
		}))
		removeControlFunctions << inputEventStream.addControl(new KeyControl(GLFW_KEY_D, 'Scroll right', { ->
			camera.transform { ->
				translate(-TICK, 0)
			}
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

	/**
	 * Ease the camera towards the given position.
	 */
	private CompletableFuture<Void> moveCameraTo(Vector3f position) {

		var startPosition = new Vector3f(camera.position)
		var nextPosition = new Vector3f(0, 0, camera.position.z())

		return new Transition(EasingFunctions::easeOutCubic, 800, { float delta ->
			camera.position = startPosition.lerp(position, delta, nextPosition)
		}).start()
	}

	@Override
	void onSceneAdded(Scene scene) {

		inputEventStream = scene.inputEventStream
		window = scene.window
		gameWindow = scene.gameWindow
		outline = new Outline(new Rectanglef(), Colour.RED, true).tap {
			transform { ->
				translate(0, 0, 0.5)
			}
		}
		scene << outline
		addControls()
	}

	@Override
	void onSceneRemoved(Scene scene) {

		clearControls()
		scene.removeChild(outline)
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

		return moveCameraTo(new Vector3f(initialPosition, 0))
	}
}
