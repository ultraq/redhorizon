/*
 * Copyright 2023, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine.graphics

import nz.net.ultraq.redhorizon.engine.input.InputSystem
import nz.net.ultraq.redhorizon.input.KeyEvent

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

/**
 * The trait for something being in an enabled/disabled state, with operations
 * to flip between the 2.
 *
 * @author Emanuel Rabina
 */
trait Switch<T extends Switch> {

	boolean enabled

	/**
	 * Change the state of this switch.
	 */
	void toggle() {

		enabled = !enabled
	}

	/**
	 * Toggle the state of this render pass with the given key.
	 */
	T toggleWith(InputSystem inputSystem, int key,
		@ClosureParams(value = SimpleType, options = 'nz.net.ultraq.redhorizon.engine.graphics.pipeline.RenderPass') Closure closure = null) {

		inputSystem.on(KeyEvent) { event ->
			if (event.isKeyPress(key)) {
				toggle()
				if (closure) {
					closure(this)
				}
			}
		}

		return (T)this
	}
}
