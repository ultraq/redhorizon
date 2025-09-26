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

package nz.net.ultraq.redhorizon.runtime.imgui

import nz.net.ultraq.eventhorizon.EventTarget
import nz.net.ultraq.redhorizon.engine.graphics.imgui.ImGuiChrome
import nz.net.ultraq.redhorizon.graphics.Framebuffer

import imgui.ImGui

/**
 * A main menu bar to add to a window.
 *
 * @author Emanuel Rabina
 */
class MainMenuBar implements ImGuiChrome, EventTarget<MainMenuBar> {

	private final List<MenuItem> optionsMenu = []

	/**
	 * Add an item to the menu bar's Options menu.
	 */
	void addOptionsMenuItem(MenuItem optionMenuItem) {

		optionsMenu << optionMenuItem
	}

	@Override
	boolean isFocused() {

		return false
	}

	@Override
	boolean isHovered() {

		return false
	}

	@Override
	void render(int dockspaceId, Framebuffer sceneFramebufferResult) {

		if (ImGui.beginMainMenuBar()) {

			if (ImGui.beginMenu('File')) {
				if (ImGui.menuItem('Exit', 'Esc')) {
					trigger(new ExitEvent())
				}
				ImGui.endMenu()
			}

			if (optionsMenu) {
				if (ImGui.beginMenu('Options')) {
//				if (ImGui.menuItem('Scanlines', 'S', shaderScanlines)) {
//					shaderScanlines = !shaderScanlines
//					trigger(new ChangeEvent(OPTIONS_SHADER_SCANLINES, shaderScanlines))
//				}
//				if (ImGui.menuItem('Sharp upscaling', 'U', shaderSharpUpscaling)) {
//					shaderSharpUpscaling = !shaderSharpUpscaling
//					trigger(new ChangeEvent(OPTIONS_SHADER_SHARP_UPSCALING, shaderSharpUpscaling))
//				}
					optionsMenu*.render()
					ImGui.endMenu()
				}
			}

			ImGui.endMainMenuBar()
		}
	}

	/**
	 * An additional menu item and the behaviour behind it.
	 */
	interface MenuItem {

		/**
		 * Draw this menu item.
		 */
		void render()
	}
}
