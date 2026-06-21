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

import imgui.ImGui
import org.joml.Vector2f
import static org.lwjgl.glfw.GLFW.*

/**
 * Display the gamepad X/Y stick positions in the debug overlay.
 *
 * @author Emanuel Rabina
 */
class GamepadAxesOverlayModule implements DebugOverlayModule {

	private final Vector2f leftStick = new Vector2f()
	private final Vector2f rightStick = new Vector2f()

	/**
	 * Constructor, read gamepad input from the window.
	 */
	GamepadAxesOverlayModule(Window window) {

		window.on(GamepadAxisEvent) { event ->
			switch (event.type()) {
				case GLFW_GAMEPAD_AXIS_LEFT_X -> leftStick.x = event.value()
				case GLFW_GAMEPAD_AXIS_LEFT_Y -> leftStick.y = event.value()
				case GLFW_GAMEPAD_AXIS_RIGHT_X -> rightStick.x = event.value()
				case GLFW_GAMEPAD_AXIS_RIGHT_Y -> rightStick.y = event.value()
			}
		}
	}

	@Override
	void render() {

		ImGui.text("Left stick: ${sprintf('%.1f', leftStick.x)}, ${sprintf('%.1f', leftStick.y)}")
		ImGui.text("Right stick: ${sprintf('%.1f', rightStick.x)}, ${sprintf('%.1f', rightStick.y)}")
	}
}
