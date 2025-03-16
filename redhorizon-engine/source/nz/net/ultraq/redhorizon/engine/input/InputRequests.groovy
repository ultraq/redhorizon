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
 * Interface for making requests of the input system.
 *
 * @author Emanuel Rabina
 */
interface InputRequests {

	/**
	 * Register an input binding with the application.
	 *
	 * @param control
	 * @return
	 *   A function that can be executed to remove the input binding that was just
	 *   added.
	 */
	RemoveControlFunction addControl(Control control)

	/**
	 * Register multiple input bindings at once.
	 *
	 * @param controls
	 * @return
	 *   A list of functions to remove the input binding, in the same order they
	 *   were added.
	 */
	default RemoveControlFunction[] addControls(Control... controls) {

		return controls.collect { control -> addControl(control) }
	}
}
