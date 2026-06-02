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

package nz.net.ultraq.redhorizon.engine.debug

import nz.net.ultraq.redhorizon.engine.graphics.GridLines
import nz.net.ultraq.redhorizon.graphics.imgui.ImGuiModule
import nz.net.ultraq.redhorizon.input.KeyBinding
import nz.net.ultraq.redhorizon.scenegraph.Scene

import static org.lwjgl.glfw.GLFW.GLFW_KEY_P

/**
 * Input binding for toggling all debug information with the {@code P} key.
 * This will toggle:
 * <ul>
 *   <li>grid lines</li>
 *   <li>collision outlines</li>
 *   <li>all ImGui debug components</li>
 * </ul>
 *
 * @author Emanuel Rabina
 */
class DebugEverythingBinding extends KeyBinding {

	DebugEverythingBinding(Scene scene) {

		super(GLFW_KEY_P, true, { ->
			var debugStore = scene.findByType(DebugStore)
			var debugModules = scene.findAll { it instanceof ImGuiModule && it.debug } as List<ImGuiModule>

			// Use this flag to determine if all on/off
			if (!debugStore.showDebugOverlay) {
				debugStore.enableAll()
				debugModules*.enable()
			}
			else {
				debugStore.disableAll()
				debugModules*.disable()
			}

			var gridLines = scene.findByType(GridLines)
			if (debugStore.showGridLines) {
				gridLines.enable()
			}
			else {
				gridLines.disable()
			}
		})
	}
}
