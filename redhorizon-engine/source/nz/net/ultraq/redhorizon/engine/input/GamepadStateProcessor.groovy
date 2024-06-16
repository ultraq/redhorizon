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

import nz.net.ultraq.redhorizon.engine.graphics.GraphicsSystem

import org.lwjgl.glfw.GLFWGamepadState
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.*

import groovy.transform.TupleConstructor
import java.nio.FloatBuffer

/**
 * A class for managing gamepad inputs and emitting them as events so that it
 * all acts the same for those on the other end of the {@link InputEventStream}.
 * <p>
 * GLFW currently doesn't have a callback system in place for joysticks, and so
 * you have to do DIY polling and handling.  See https://github.com/glfw/glfw/issues/601,
 * which is unlikely to be solved any time soon.
 * <p>
 * Joystick/Gamepad processing is currently restricted to the same thread as the
 * GLFW context, so this class is only used from the {@link GraphicsSystem}.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class GamepadStateProcessor {

	private static final Logger logger = LoggerFactory.getLogger(GamepadStateProcessor)

	final InputEventStream inputEventStream

	@Lazy
	private GLFWGamepadState gamepadState = { GLFWGamepadState.create() }()

	/**
	 * Check for any changes to the joystick/gamepad state and emit events for
	 * them.  Called after {@code glfwPollEvents}.
	 */
	void process() {

		if (glfwJoystickIsGamepad(GLFW_JOYSTICK_1)) {
			glfwGetGamepadState(GLFW_JOYSTICK_1, gamepadState)

			var axes = gamepadState.axes()
			processAxis(axes, GLFW_GAMEPAD_AXIS_LEFT_X, 'Gamepad left stick X: {}')
			processAxis(axes, GLFW_GAMEPAD_AXIS_LEFT_Y, 'Gamepad left stick Y: {}')
			processAxis(axes, GLFW_GAMEPAD_AXIS_RIGHT_X, 'Gamepad right stick X: {}')
			processAxis(axes, GLFW_GAMEPAD_AXIS_RIGHT_Y, 'Gamepad right stick Y: {}')
		}
	}

	/**
	 * Process a single axis.
	 */
	private void processAxis(FloatBuffer axes, int type, String logMessage) {

		var value = axes.get(type)
		logger.debug(logMessage, sprintf('%.2f', value))
		inputEventStream.trigger(new GamepadAxisEvent(type, value <= -0.2f || 0.2f <= value ? value : 0f))
	}
}
