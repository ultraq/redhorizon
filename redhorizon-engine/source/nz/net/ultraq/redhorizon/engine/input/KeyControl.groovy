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


import org.lwjgl.glfw.GLFW
import static org.lwjgl.glfw.GLFW.GLFW_PRESS
import static org.lwjgl.glfw.GLFW.GLFW_REPEAT

import java.lang.reflect.Modifier

/**
 * Keyboard-specific control class.
 *
 * @author Emanuel Rabina
 */
class KeyControl extends Control<KeyEvent> {

	private final int key
	private Closure handler

	/**
	 * Constructor, build a key-handling control around the given key.
	 *
	 * @param key
	 * @param name
	 * @param handler
	 */
	KeyControl(int key, String name, Closure handler) {

		super(KeyEvent, name, determineKeyBindingName(key))
		this.key = key
		this.handler = handler
	}

	/**
	 * Return a string representing the name of the key binding.
	 *
	 * @param key
	 * @return
	 */
	private static String determineKeyBindingName(int key) {

		var field = GLFW.getDeclaredFields().find { field ->
			return Modifier.isStatic(field.modifiers) && field.name.startsWith("GLFW_KEY_") && field.getInt(null) == key
		}
		if (field) {
			return field.name.substring(9).toLowerCase().capitalize()
		}
		return key.toString()
	}

	@Override
	void handleEvent(KeyEvent event) {

		if ((event.action == GLFW_PRESS || event.action == GLFW_REPEAT) && event.key == key) {
			handler()
		}
	}
}
