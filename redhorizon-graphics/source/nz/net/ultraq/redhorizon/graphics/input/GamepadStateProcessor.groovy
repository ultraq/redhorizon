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

package nz.net.ultraq.redhorizon.graphics.input

import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLWindow
import nz.net.ultraq.redhorizon.input.GamepadAxisEvent
import nz.net.ultraq.redhorizon.input.GamepadButtonEvent

import org.lwjgl.glfw.GLFWGamepadState
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.*
import static org.lwjgl.system.MemoryStack.stackASCII

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
class GamepadStateProcessor {

	private static final Logger logger = LoggerFactory.getLogger(GamepadStateProcessor)
	private static final int[] AXES = [
		GLFW_GAMEPAD_AXIS_LEFT_X,
		GLFW_GAMEPAD_AXIS_LEFT_Y,
		GLFW_GAMEPAD_AXIS_RIGHT_X,
		GLFW_GAMEPAD_AXIS_RIGHT_Y,
		GLFW_GAMEPAD_AXIS_LEFT_TRIGGER,
		GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER,
	]
	private static final int[] BUTTONS = [
		GLFW_GAMEPAD_BUTTON_A,
		GLFW_GAMEPAD_BUTTON_B,
		GLFW_GAMEPAD_BUTTON_X,
		GLFW_GAMEPAD_BUTTON_Y,
		GLFW_GAMEPAD_BUTTON_LEFT_BUMPER,
		GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER,
		GLFW_GAMEPAD_BUTTON_BACK,
		GLFW_GAMEPAD_BUTTON_START,
		GLFW_GAMEPAD_BUTTON_GUIDE,
		GLFW_GAMEPAD_BUTTON_LEFT_THUMB,
		GLFW_GAMEPAD_BUTTON_RIGHT_THUMB,
		GLFW_GAMEPAD_BUTTON_DPAD_UP,
		GLFW_GAMEPAD_BUTTON_DPAD_RIGHT,
		GLFW_GAMEPAD_BUTTON_DPAD_DOWN,
		GLFW_GAMEPAD_BUTTON_DPAD_LEFT,
	]

	final OpenGLWindow window
	@Lazy
	private GLFWGamepadState gamepadState = { GLFWGamepadState.create() }()
	private final Map<Integer, Boolean> buttonStates = new ConcurrentHashMap<>()

	/**
	 * Constructor, sets up gamepad processing.
	 */
	GamepadStateProcessor(OpenGLWindow window) {

		this.window = window

		// Adding an entry for Xbox Elite Series 2 recognized as Xbox Wireless Controller in macOS 26
		var additionalGamepadDB = getResourceAsText('nz/net/ultraq/redhorizon/graphics/input/AdditionalGamepadDB.txt')
		var buffer = stackASCII(additionalGamepadDB)
		glfwUpdateGamepadMappings(buffer)
		logger.debug('Gamepad ID / Name: {}', glfwGetJoystickGUID(GLFW_JOYSTICK_1), glfwGetJoystickName(GLFW_JOYSTICK_1))
	}

	/**
	 * Check for any changes to the joystick/gamepad state and emit events for
	 * them.  Called after {@code glfwPollEvents}.
	 */
	void process() {

		if (glfwJoystickIsGamepad(GLFW_JOYSTICK_1)) {
			glfwGetGamepadState(GLFW_JOYSTICK_1, gamepadState)
			var axes = gamepadState.axes()
			AXES.each { axis -> processAxis(axes, axis) }
			var buttons = gamepadState.buttons()
			BUTTONS.each { button -> processButton(buttons, button) }
		}
	}

	/**
	 * Process a single axis.
	 */
	private void processAxis(FloatBuffer axes, int type) {

		var value = axes.get(type)
		window.trigger(new GamepadAxisEvent(type, value))
	}

	/**
	 * Process a single button.
	 */
	private void processButton(ByteBuffer buttons, int button) {

		var pressed = buttons.get(button) == GLFW_PRESS
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
