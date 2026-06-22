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

package nz.net.ultraq.redhorizon.graphics.opengl

import nz.net.ultraq.redhorizon.input.GamepadAxisEvent
import nz.net.ultraq.redhorizon.input.GamepadButtonEvent

import org.lwjgl.glfw.GLFWGamepadState
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.*

import groovy.transform.TupleConstructor
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.util.concurrent.ConcurrentHashMap

/**
 * A class for managing gamepad inputs and emitting them as events so that it
 * all acts the same for those on the other end of Red Horizon's input system.
 * <p>
 * GLFW currently doesn't have a callback system in place for joysticks, and so
 * you have to do DIY polling and handling.  See https://github.com/glfw/glfw/issues/601,
 * which is unlikely to be solved any time soon.
 * <p>
 * Joystick/Gamepad processing is currently restricted to the same thread as the
 * GLFW context, so this class can only used in the same thread as the graphics
 * one.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class GamepadStateProcessor {

	private static final Logger logger = LoggerFactory.getLogger(GamepadStateProcessor)

	// Testing has shown that not all of the buttons in GLFW match exactly to the
	// buttons on a gamepad, so this class is to remap the values experienced to
	// the GLFW values so that GLFW can continue to be used as expected.
	// Note that this might only be a macOS thing: gotta test in Windows too.
	private static final Map<Integer, Integer> MAPPING = [
		(GLFW_GAMEPAD_BUTTON_A): 0,
		(GLFW_GAMEPAD_BUTTON_B): 1,
		(GLFW_GAMEPAD_BUTTON_X): 3,
		(GLFW_GAMEPAD_BUTTON_Y): 4,
		(GLFW_GAMEPAD_BUTTON_LEFT_BUMPER): 6,
		(GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER): 7,
		(GLFW_GAMEPAD_BUTTON_BACK): 10,
		(GLFW_GAMEPAD_BUTTON_START): 11,
		(GLFW_GAMEPAD_BUTTON_GUIDE): 12,
		(GLFW_GAMEPAD_BUTTON_LEFT_THUMB): 13,
		(GLFW_GAMEPAD_BUTTON_RIGHT_THUMB): 14,
		(GLFW_GAMEPAD_BUTTON_DPAD_UP): 23,
		(GLFW_GAMEPAD_BUTTON_DPAD_RIGHT): 24,
		(GLFW_GAMEPAD_BUTTON_DPAD_DOWN): 25,
		(GLFW_GAMEPAD_BUTTON_DPAD_LEFT): 26
	]

	final OpenGLWindow window
	@Lazy
	private GLFWGamepadState gamepadState = { GLFWGamepadState.create() }()
	private final Map<Integer, Boolean> buttonStates = new ConcurrentHashMap<>()

	/**
	 * Check for any changes to the joystick/gamepad state and emit events for
	 * them.  Called after {@code glfwPollEvents}.
	 */
	void process() {

		if (glfwJoystickIsGamepad(GLFW_JOYSTICK_1)) {
			glfwGetGamepadState(GLFW_JOYSTICK_1, gamepadState)
			processAxes(gamepadState.axes())
			processButtons(gamepadState.buttons())
		}
		// Gamepad mappings not working on macOS, have to use joystick 😢
		else if (glfwJoystickPresent(GLFW_JOYSTICK_1)) {
			processAxes(glfwGetJoystickAxes(GLFW_JOYSTICK_1))
			processButtons(glfwGetJoystickButtons(GLFW_JOYSTICK_1))
		}
	}

	/**
	 * Process a set of axes.
	 */
	private void processAxes(FloatBuffer axes) {

		processAxis(axes, GLFW_GAMEPAD_AXIS_LEFT_X)
		processAxis(axes, GLFW_GAMEPAD_AXIS_LEFT_Y)
		processAxis(axes, GLFW_GAMEPAD_AXIS_RIGHT_X)
		processAxis(axes, GLFW_GAMEPAD_AXIS_RIGHT_Y)
		processAxis(axes, GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER)
	}

	/**
	 * Process a single axis.
	 */
	private void processAxis(FloatBuffer axes, int type) {

		var value = axes.get(type)
		window.trigger(new GamepadAxisEvent(type, value))
	}

	/**
	 * Process a set of buttons.
	 */
	private void processButtons(ByteBuffer buttons) {

		var buttonValues = []
		for (var i = 0; i < buttons.limit(); i++) {
			buttonValues << buttons.get(i)
		}

		processButton(buttons, GLFW_GAMEPAD_BUTTON_A, MAPPING[GLFW_GAMEPAD_BUTTON_A])
		processButton(buttons, GLFW_GAMEPAD_BUTTON_B, MAPPING[GLFW_GAMEPAD_BUTTON_B])
		processButton(buttons, GLFW_GAMEPAD_BUTTON_X, MAPPING[GLFW_GAMEPAD_BUTTON_X])
		processButton(buttons, GLFW_GAMEPAD_BUTTON_Y, MAPPING[GLFW_GAMEPAD_BUTTON_Y])
		processButton(buttons, GLFW_GAMEPAD_BUTTON_LEFT_BUMPER, MAPPING[GLFW_GAMEPAD_BUTTON_LEFT_BUMPER])
		processButton(buttons, GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER, MAPPING[GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER])
		processButton(buttons, GLFW_GAMEPAD_BUTTON_BACK, MAPPING[GLFW_GAMEPAD_BUTTON_BACK])
		processButton(buttons, GLFW_GAMEPAD_BUTTON_START, MAPPING[GLFW_GAMEPAD_BUTTON_START])
		processButton(buttons, GLFW_GAMEPAD_BUTTON_GUIDE, MAPPING[GLFW_GAMEPAD_BUTTON_GUIDE])
		processButton(buttons, GLFW_GAMEPAD_BUTTON_LEFT_THUMB, MAPPING[GLFW_GAMEPAD_BUTTON_LEFT_THUMB])
		processButton(buttons, GLFW_GAMEPAD_BUTTON_RIGHT_THUMB, MAPPING[GLFW_GAMEPAD_BUTTON_RIGHT_THUMB])
		processButton(buttons, GLFW_GAMEPAD_BUTTON_DPAD_UP, MAPPING[GLFW_GAMEPAD_BUTTON_DPAD_UP])
		processButton(buttons, GLFW_GAMEPAD_BUTTON_DPAD_RIGHT, MAPPING[GLFW_GAMEPAD_BUTTON_DPAD_RIGHT])
		processButton(buttons, GLFW_GAMEPAD_BUTTON_DPAD_DOWN, MAPPING[GLFW_GAMEPAD_BUTTON_DPAD_DOWN])
		processButton(buttons, GLFW_GAMEPAD_BUTTON_DPAD_LEFT, MAPPING[GLFW_GAMEPAD_BUTTON_DPAD_LEFT])
	}

	/**
	 * Process a single button.
	 *
	 * @param button
	 *   The GLFW value for the button.
	 * @param index
	 *   Actual observed value for the button.
	 */
	private void processButton(ByteBuffer buttons, int button, int index) {

		var pressed = buttons.get(index) == GLFW_PRESS
		if (pressed) {
			window.trigger(new GamepadButtonEvent(button, true))
			buttonStates[button] = true
		}
		// Only trigger a release event if the button was previously pressed,
		// otherwise we'd constantly be firing release events because of the polling
		// nature of GLFW's gamepad handling.
		else if (buttonStates[button]) {
			window.trigger(new GamepadButtonEvent(button, false))
			buttonStates[button] = false
		}
	}
}
