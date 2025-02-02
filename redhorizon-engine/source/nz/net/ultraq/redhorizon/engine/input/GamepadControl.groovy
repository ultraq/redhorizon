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

package nz.net.ultraq.redhorizon.engine.input

import static org.lwjgl.glfw.GLFW.*

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

/**
 * Gamepad-specific control class.
 *
 * @author Emanuel Rabina
 */
class GamepadControl extends Control<GamepadAxisEvent> {

	private final int type
	private final Closure handler

	/**
	 * Create a new gamepad control to act on the given gamepad axis events.
	 */
	GamepadControl(int type, String name,
		@ClosureParams(value = SimpleType, options = 'float') Closure handler) {

		super(GamepadAxisEvent, name, determineBindingName(type))
		this.type = type
		this.handler = handler
	}

	/**
	 * Return a string representation of the axis binding.
	 */
	private static String determineBindingName(int type) {

		return switch (type) {
			case GLFW_GAMEPAD_AXIS_LEFT_X -> 'Left stick X axis'
			case GLFW_GAMEPAD_AXIS_LEFT_Y -> 'Left stick Y axis'
			case GLFW_GAMEPAD_AXIS_RIGHT_X -> 'Right stick X axis'
			case GLFW_GAMEPAD_AXIS_RIGHT_Y -> 'Right stick Y axis'
			case GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER -> 'Right trigger'
			default -> '(unknown)'
		}
	}

	@Override
	void handleEvent(GamepadAxisEvent event) {

		if (event.type == type) {
			handler(event.value)
		}
	}
}
