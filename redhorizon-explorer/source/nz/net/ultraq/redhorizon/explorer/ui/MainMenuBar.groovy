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

import nz.net.ultraq.redhorizon.explorer.ExplorerScene
import nz.net.ultraq.redhorizon.explorer.actions.CyclePaletteAction
import nz.net.ultraq.redhorizon.explorer.ui.actions.ExitApplicationAction
import nz.net.ultraq.redhorizon.explorer.ui.actions.ToggleTouchpadInputAction
import nz.net.ultraq.redhorizon.graphics.Window
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
class MainMenuBar implements ImGuiModule {

	final Window window
	final ExplorerScene scene
	final UiController uiController // TODO: Some state/options object instead of the controller

	@Override
	void render(ImGuiContext context) {

		if (ImGui.beginMainMenuBar()) {

			if (ImGui.beginMenu('File')) {
				if (ImGui.menuItem('Exit', 'Esc')) {
					new ExitApplicationAction(window).exit()
				}
				ImGui.endMenu()
			}

			if (ImGui.beginMenu('Options')) {
				if (ImGui.menuItem('Touchpad input', null, uiController.touchpadInput)) {
					new ToggleTouchpadInputAction(scene, uiController).toggle()
				}
				if (ImGui.menuItem('Cycle palette', 'P')) {
					new CyclePaletteAction(scene).cyclePalette()
				}
				ImGui.endMenu()
			}

			ImGui.endMainMenuBar()
		}
	}
}
