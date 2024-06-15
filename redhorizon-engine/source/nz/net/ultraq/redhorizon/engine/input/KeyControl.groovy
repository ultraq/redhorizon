/*
 * Copyright 2022, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import java.lang.reflect.Modifier

/**
 * Keyboard-specific control class.
 *
 * @author Emanuel Rabina
 */
class KeyControl extends Control<KeyEvent> {

	private final int modifier
	private final int key
	private final boolean includeRepeat
	private final Closure pressHandler
	private final Closure releaseHandler

	KeyControl(int key, String name, Closure pressHandler, boolean includeRepeat) {

		this(-1, key, name, pressHandler, includeRepeat, null)
	}

	KeyControl(int key, String name, Closure pressHandler, Closure releaseHandler = null) {

		this(-1, key, name, pressHandler, false, releaseHandler)
	}

	KeyControl(int modifier, int key, String name, Closure pressHandler, boolean includeRepeat, Closure releaseHandler = null) {

		super(KeyEvent, name, determineBindingName(modifier, key))
		this.modifier = modifier
		this.key = key
		this.includeRepeat = includeRepeat
		this.pressHandler = pressHandler
		this.releaseHandler = releaseHandler
	}

	/**
	 * Return a string representing the name of the key binding.
	 */
	private static String determineBindingName(int modifier, int key) {

		var modifierName = determineModifierName(modifier)
		var buttonField = glfwFields.find { field ->
			return Modifier.isStatic(field.modifiers) && field.name.startsWith("GLFW_KEY_") && field.getInt(null) == key
		}
		return buttonField ?
			modifierName + buttonField.name.substring(9).toLowerCase().capitalize() :
			key.toString()
	}

	@Override
	void handleEvent(KeyEvent event) {

		if (event.key == key) {
			if (event.action == GLFW_PRESS || (event.action == GLFW_REPEAT && includeRepeat)) {
				pressHandler()
			}
			else if (event.action == GLFW_RELEASE) {
				releaseHandler?.call()
			}
		}
	}
}
