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

package nz.net.ultraq.redhorizon.engine.input

/**
 * Any object in a scene that can accept player input.
 *
 * @author Emanuel Rabina
 */
interface InputHandler {

	/**
	 * Called during the input stage of the game loop to allow this object to
	 * respond to player input.
	 *
	 * @return Whether or not the input was handled in this object.
	 */
	boolean input(InputEvent inputEvent)

	/**
	 * Convenience method for performing some action in {@code closure} and
	 * returning {@code true}, as a way to signal in {@link #input} that an input
	 * event was handled.
	 */
	default boolean inputHandled(Closure closure) {

		closure()
		return true
	}
}
