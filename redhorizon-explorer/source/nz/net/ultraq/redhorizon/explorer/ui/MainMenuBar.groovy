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

package nz.net.ultraq.redhorizon.explorer.ui

import nz.net.ultraq.eventhorizon.EventTarget
import nz.net.ultraq.redhorizon.graphics.imgui.ImGuiContext
import nz.net.ultraq.redhorizon.graphics.imgui.ImGuiModule

import imgui.ImGui

import groovy.transform.TupleConstructor

/**
 * Menu bar for the Explorer.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class MainMenuBar implements ImGuiModule, EventTarget<MainMenuBar> {

	boolean touchpadInput

	@Override
	void render(ImGuiContext context) {

		if (ImGui.beginMainMenuBar()) {

			if (ImGui.beginMenu('File')) {
				if (ImGui.menuItem('Exit', 'Esc')) {
					trigger(new ExitEvent())
				}
				ImGui.endMenu()
			}

			if (ImGui.beginMenu('Options')) {
				if (ImGui.menuItem('Touchpad input', null, touchpadInput)) {
					touchpadInput = !touchpadInput
					trigger(new TouchpadInputEvent(touchpadInput))
				}
				if (ImGui.menuItem('Cycle palette', 'P')) {
					trigger(new CyclePaletteEvent())
				}
				ImGui.endMenu()
			}

			ImGui.endMainMenuBar()
		}
	}
}
