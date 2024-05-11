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

import java.lang.reflect.Modifier

/**
 * Mouse-specific control class.
 *
 * @author Emanuel Rabina
 */
class MouseControl extends Control<MouseButtonEvent> {

	private final int modifier
	private final int button
	private Closure handler

	MouseControl(int button, String name, Closure handler) {

		this(0, button, name, handler)
	}

	MouseControl(int modifier, int button, String name, Closure handler) {

		super(MouseButtonEvent, name, determineBindingName(modifier, button))
		this.modifier = modifier
		this.button = button
		this.handler = handler
	}

	/**
	 * Return a string representing the name of the key binding.
	 */
	private static String determineBindingName(int modifier, int button) {

		var modifierName = determineModifierName(modifier)
		var buttonField = glfwFields.find { field ->
			return Modifier.isStatic(field.modifiers) && field.name.startsWith("GLFW_MOUSE_BUTTON_") && field.getInt(null) == button
		}
		return buttonField ?
			"${modifierName}Mouse ${buttonField.name.substring(18).toLowerCase().capitalize()}" :
			button.toString()
	}

	@Override
	void handleEvent(MouseButtonEvent event) {

		if (event.button == button && event.mods == modifier) {
			handler()
		}
	}
}
