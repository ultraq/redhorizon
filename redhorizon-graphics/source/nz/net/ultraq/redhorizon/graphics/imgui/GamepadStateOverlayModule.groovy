/*
 * Copyright 2026, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.graphics.imgui

import nz.net.ultraq.redhorizon.graphics.Window
import nz.net.ultraq.redhorizon.input.GamepadAxisEvent
import nz.net.ultraq.redhorizon.input.GamepadButtonEvent

import imgui.ImGui
import org.joml.Vector2f
import static org.lwjgl.glfw.GLFW.*

import java.util.concurrent.ConcurrentHashMap

/**
 * Display the gamepad axes and button states in the debug overlay.
 *
 * @author Emanuel Rabina
 */
class GamepadStateOverlayModule implements DebugOverlayModule {

	private final Vector2f leftStick = new Vector2f()
	private final Vector2f rightStick = new Vector2f()
	private float leftTrigger = 0f
	private float rightTrigger = 0f
	private final Map<Integer, Boolean> buttonStates = new ConcurrentHashMap<>()
	private final Map<Integer, String> buttonNames = [
		(GLFW_GAMEPAD_BUTTON_A): "A",
		(GLFW_GAMEPAD_BUTTON_B): "B",
		(GLFW_GAMEPAD_BUTTON_X): "X",
		(GLFW_GAMEPAD_BUTTON_Y): "Y",
		(GLFW_GAMEPAD_BUTTON_LEFT_BUMPER): "LB",
		(GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER): "RB",
		(GLFW_GAMEPAD_BUTTON_BACK): "Back",
		(GLFW_GAMEPAD_BUTTON_START): "Start",
		(GLFW_GAMEPAD_BUTTON_GUIDE): "Guide",
		(GLFW_GAMEPAD_BUTTON_LEFT_THUMB): "LT",
		(GLFW_GAMEPAD_BUTTON_RIGHT_THUMB): "RT",
		(GLFW_GAMEPAD_BUTTON_DPAD_UP): "Up",
		(GLFW_GAMEPAD_BUTTON_DPAD_RIGHT): "Right",
		(GLFW_GAMEPAD_BUTTON_DPAD_DOWN): "Down",
		(GLFW_GAMEPAD_BUTTON_DPAD_LEFT): "Left"
	]

	/**
	 * Constructor, read gamepad input from the window.
	 */
	GamepadStateOverlayModule(Window window) {

		window.on(GamepadAxisEvent) { event ->
			switch (event.type()) {
				case GLFW_GAMEPAD_AXIS_LEFT_X -> leftStick.x = event.value()
				case GLFW_GAMEPAD_AXIS_LEFT_Y -> leftStick.y = event.value()
				case GLFW_GAMEPAD_AXIS_RIGHT_X -> rightStick.x = event.value()
				case GLFW_GAMEPAD_AXIS_RIGHT_Y -> rightStick.y = event.value()
				case GLFW_GAMEPAD_AXIS_LEFT_TRIGGER -> leftTrigger = event.value()
				case GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER -> rightTrigger = event.value()
			}
		}
		window.on(GamepadButtonEvent) { event ->
			buttonStates[event.button()] = event.pressed()
		}
	}

	@Override
	void render() {

		ImGui.text("Left stick: ${sprintf('%.1f', leftStick.x)}, ${sprintf('%.1f', leftStick.y)}")
		ImGui.text("Right stick: ${sprintf('%.1f', rightStick.x)}, ${sprintf('%.1f', rightStick.y)}")
		ImGui.text("Left trigger: ${sprintf('%.1f', leftTrigger)}")
		ImGui.text("Right trigger: ${sprintf('%.1f', rightTrigger)}")
		var buttonsPressed = buttonNames.inject([]) { acc, key, name ->
			if (buttonStates[key]) {
				acc << name
			}
			return acc
		}
		ImGui.text("Buttons pressed: ${buttonsPressed.join(',')}")
	}
}
