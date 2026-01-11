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

package nz.net.ultraq.redhorizon.input

/**
 * Event for keyboard input.
 *
 * @author Emanuel Rabina
 */
record KeyEvent(int key, int mods, boolean pressed) implements InputEvent {

	/**
	 * Return whether this event is a key press for the given key.
	 */
	boolean keyPressed(int key) {

		return this.key == key && pressed
	}

	/**
	 * Return whether this event is a key release for the given key.
	 */
	boolean keyReleased(int key) {

		return this.key == key && !pressed
	}
}
