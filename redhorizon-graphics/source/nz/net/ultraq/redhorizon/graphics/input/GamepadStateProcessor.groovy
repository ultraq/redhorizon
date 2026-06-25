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

	final OpenGLWindow window
	@Lazy
	private GLFWGamepadState gamepadState = { GLFWGamepadState.create() }()
	private final Map<Integer, Boolean> buttonStates = new ConcurrentHashMap<>()

	/**
	 * Constructor, sets up gamepad processing.
	 */
	GamepadStateProcessor(OpenGLWindow window) {

		this.window = window

		// Update the controller DB - the one in GLFW hasn't been updated since 2021 😮
		// https://github.com/glfw/glfw/pull/2745
//		stackPush().withCloseable { stack ->
//		var gameControllerDb = getResourceAsText('nz/net/ultraq/redhorizon/graphics/input/gamecontrollerdb.txt')
//		var gameControllerDb = '030000005e040000050b000003090000,Xbox Elite Controller Series 2,a:b0,b:b1,back:b31,dpdown:h0.4,dpleft:h0.8,dpright:h0.2,dpup:h0.1,guide:b53,leftshoulder:b6,leftstick:b13,lefttrigger:a6,leftx:a0,lefty:a1,rightshoulder:b7,rightstick:b14,righttrigger:a5,rightx:a2,righty:a3,start:b11,x:b3,y:b4,platform:Mac OS X'
//		var buffer = stackASCII(gameControllerDb)
//			var buffer = stack.malloc(gameControllerDb.length() + Long.BYTES)
//				.put(gameControllerDb.getBytes())
//				.putLong(NULL)
//				.flip()
//		glfwUpdateGamepadMappings(buffer)
//		}

		logger.debug('Gamepad name: {} / {}', glfwGetJoystickName(GLFW_JOYSTICK_1), glfwGetGamepadName(GLFW_JOYSTICK_1))
	}

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
		processAxis(axes, GLFW_GAMEPAD_AXIS_LEFT_TRIGGER)
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

		// Testing has shown that not all of the buttons in GLFW match exactly to
		// the buttons on a gamepad on macOS, so this remaps the GLFW names to the
		// values experienced so that we can continue to use GLFW constants.
//		if (System.isMacOs()) {
//			processButton(buttons, GLFW_GAMEPAD_BUTTON_A, 0)
//			processButton(buttons, GLFW_GAMEPAD_BUTTON_B, 1)
//			processButton(buttons, GLFW_GAMEPAD_BUTTON_X, 3)
//			processButton(buttons, GLFW_GAMEPAD_BUTTON_Y, 4)
//			processButton(buttons, GLFW_GAMEPAD_BUTTON_LEFT_BUMPER, 6)
//			processButton(buttons, GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER, 7)
//			processButton(buttons, GLFW_GAMEPAD_BUTTON_BACK, 10)
//			processButton(buttons, GLFW_GAMEPAD_BUTTON_START, 11)
//			processButton(buttons, GLFW_GAMEPAD_BUTTON_GUIDE, 12)
//			processButton(buttons, GLFW_GAMEPAD_BUTTON_LEFT_THUMB, 13)
//			processButton(buttons, GLFW_GAMEPAD_BUTTON_RIGHT_THUMB, 14)
//			processButton(buttons, GLFW_GAMEPAD_BUTTON_DPAD_UP, 23)
//			processButton(buttons, GLFW_GAMEPAD_BUTTON_DPAD_RIGHT, 24)
//			processButton(buttons, GLFW_GAMEPAD_BUTTON_DPAD_DOWN, 25)
//			processButton(buttons, GLFW_GAMEPAD_BUTTON_DPAD_LEFT, 26)
//		}
//		else {
		processButton(buttons, GLFW_GAMEPAD_BUTTON_A, GLFW_GAMEPAD_BUTTON_A)
		processButton(buttons, GLFW_GAMEPAD_BUTTON_B, GLFW_GAMEPAD_BUTTON_B)
		processButton(buttons, GLFW_GAMEPAD_BUTTON_X, GLFW_GAMEPAD_BUTTON_X)
		processButton(buttons, GLFW_GAMEPAD_BUTTON_Y, GLFW_GAMEPAD_BUTTON_Y)
		processButton(buttons, GLFW_GAMEPAD_BUTTON_LEFT_BUMPER, GLFW_GAMEPAD_BUTTON_LEFT_BUMPER)
		processButton(buttons, GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER, GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER)
		processButton(buttons, GLFW_GAMEPAD_BUTTON_BACK, GLFW_GAMEPAD_BUTTON_BACK)
		processButton(buttons, GLFW_GAMEPAD_BUTTON_START, GLFW_GAMEPAD_BUTTON_START)
		processButton(buttons, GLFW_GAMEPAD_BUTTON_GUIDE, GLFW_GAMEPAD_BUTTON_GUIDE)
		processButton(buttons, GLFW_GAMEPAD_BUTTON_LEFT_THUMB, GLFW_GAMEPAD_BUTTON_LEFT_THUMB)
		processButton(buttons, GLFW_GAMEPAD_BUTTON_RIGHT_THUMB, GLFW_GAMEPAD_BUTTON_RIGHT_THUMB)
		processButton(buttons, GLFW_GAMEPAD_BUTTON_DPAD_UP, GLFW_GAMEPAD_BUTTON_DPAD_UP)
		processButton(buttons, GLFW_GAMEPAD_BUTTON_DPAD_RIGHT, GLFW_GAMEPAD_BUTTON_DPAD_RIGHT)
		processButton(buttons, GLFW_GAMEPAD_BUTTON_DPAD_DOWN, GLFW_GAMEPAD_BUTTON_DPAD_DOWN)
		processButton(buttons, GLFW_GAMEPAD_BUTTON_DPAD_LEFT, GLFW_GAMEPAD_BUTTON_DPAD_LEFT)
//		}
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
