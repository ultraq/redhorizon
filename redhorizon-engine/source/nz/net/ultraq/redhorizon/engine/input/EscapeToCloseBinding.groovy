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

import nz.net.ultraq.redhorizon.graphics.Window
import nz.net.ultraq.redhorizon.input.InputBinding
import nz.net.ultraq.redhorizon.input.InputEventHandler

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE

import groovy.transform.TupleConstructor

/**
 * Bind the {@code ESC} key to closing the window.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class EscapeToCloseBinding implements InputBinding {

	final Window window

	@Override
	void process(InputEventHandler input) {

		if (input.keyPressed(GLFW_KEY_ESCAPE, true)) {
			window.shouldClose(true)
		}
	}
}
