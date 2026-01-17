/*
 * Copyright 2026, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.explorer.ui.actions

import nz.net.ultraq.redhorizon.explorer.ExplorerScene
import nz.net.ultraq.redhorizon.explorer.ui.TouchpadInputEvent
import nz.net.ultraq.redhorizon.explorer.ui.UiController

import groovy.transform.TupleConstructor

/**
 * Command object for changing the touchpad input option.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class ToggleTouchpadInputAction {

	final ExplorerScene scene
	final UiController uiController

	/**
	 * Switch the value of the option to the given value.
	 */
	void toggle() {

		uiController.touchpadInput = !uiController.touchpadInput
		scene.trigger(new TouchpadInputEvent(uiController.touchpadInput))
	}
}
