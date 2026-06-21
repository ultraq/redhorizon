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

import org.lwjgl.glfw.GLFWGamepadState
import static org.lwjgl.glfw.GLFW.*

import groovy.transform.TupleConstructor
import java.nio.FloatBuffer

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

	final OpenGLWindow window
	@Lazy
	private GLFWGamepadState gamepadState = { GLFWGamepadState.create() }()

	/**
	 * Check for any changes to the joystick/gamepad state and emit events for
	 * them.  Called after {@code glfwPollEvents}.
	 */
	void process() {

		if (glfwJoystickIsGamepad(GLFW_JOYSTICK_1)) {
			glfwGetGamepadState(GLFW_JOYSTICK_1, gamepadState)
			processAxes(gamepadState.axes())
		}
		// Gamepad mappings not working on macOS, have to use joystick 😢
		else if (glfwJoystickPresent(GLFW_JOYSTICK_1)) {
			processAxes(glfwGetJoystickAxes(GLFW_JOYSTICK_1))
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
}
