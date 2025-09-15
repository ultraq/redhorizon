/*
 * Copyright 2021, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import static org.lwjgl.glfw.GLFW.*

import groovy.transform.TupleConstructor

/**
 * Event for mouse input
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class MouseButtonEvent implements InputEvent {

	final int button
	final int action
	final int mods

	/**
	 * Return whether this event is for a button press for the given button.
	 */
	boolean buttonPressed(int button) {

		return this.button == button && action == GLFW_PRESS
	}

	/**
	 * Return whether this event is for a button release for the given button.
	 */
	boolean buttonReleased(int button) {

		return this.button == button && action == GLFW_RELEASE
	}
}
