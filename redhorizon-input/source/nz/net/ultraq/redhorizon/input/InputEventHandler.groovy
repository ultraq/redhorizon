/*
 * Copyright 2025, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.input

import org.joml.Vector2f
import static org.lwjgl.glfw.GLFW.*

import java.util.concurrent.ConcurrentHashMap

/**
 * Given one or more {@link InputSource}s, this class can be used to easily
 * query the state of input from those sources so that a game can act
 * accordingly.
 *
 * @author Emanuel Rabina
 */
class InputEventHandler {

	private final Map<Integer, Boolean> keyPressedStates = new ConcurrentHashMap<>()
	private final Map<Integer, Boolean> mouseButtonPressedStates = new ConcurrentHashMap<>()
	private final Vector2f cursorPosition = new Vector2f()

	/**
	 * Add another input source to listen to.
	 */
	InputEventHandler addInputSource(InputSource inputSource) {

		inputSource.on(InputEvent) { event ->
			if (event instanceof KeyEvent) {
				if (event.action() == GLFW_PRESS) {
					keyPressedStates[event.key()] = true
				}
				else if (event.action() == GLFW_RELEASE) {
					keyPressedStates[event.key()] = false
				}
			}
			else if (event instanceof MouseButtonEvent) {
				mouseButtonPressedStates[event.button()] = event.action() == GLFW_PRESS
			}
			else if (event instanceof CursorPositionEvent) {
				cursorPosition.set(event.xPos(), event.yPos())
			}
		}
		return this
	}

	/**
	 * Return the current cursor position.
	 */
	Vector2f cursorPosition() {

		return cursorPosition
	}

	/**
	 * Return whether the given key is currently pressed.
	 *
	 * @param key
	 * @param once
	 *   If {@code true}, then this method will only return {@code true} the first
	 *   time this method is called and the key is pressed.  Further calls for the
	 *   same key will return {@code false} until it is pressed again.
	 */
	boolean keyPressed(int key, boolean once = false) {

		var result = keyPressedStates[key]
		if (result && once) {
			keyPressedStates[key] = false
		}
		return result
	}

	/**
	 * Return whether the given mouse button is currently pressed.
	 */
	boolean mouseButtonPressed(int button) {

		return mouseButtonPressedStates[button]
	}
}
