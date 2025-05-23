/*
 * Copyright 2020, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import groovy.transform.ToString
import groovy.transform.TupleConstructor

/**
 * Event for keyboard input.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
@ToString(includeNames = true, includePackage = false)
class KeyEvent extends InputEvent {

	final int key
	final int scancode
	final int action
	final int mods

	/**
	 * Return whether this event is a key press for the given key.
	 */
	boolean isKeyPress(int key, boolean includeRepeat = false) {

		return this.key == key && action == GLFW_PRESS || (action == GLFW_REPEAT && includeRepeat)
	}

	/**
	 * Return whether this event is a key release for the given key.
	 */
	boolean isKeyRelease(int key) {

		return this.key == key && action == GLFW_RELEASE
	}
}
