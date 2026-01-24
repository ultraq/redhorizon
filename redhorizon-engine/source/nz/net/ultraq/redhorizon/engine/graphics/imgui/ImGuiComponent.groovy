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

package nz.net.ultraq.redhorizon.engine.graphics.imgui

import nz.net.ultraq.eventhorizon.Event
import nz.net.ultraq.eventhorizon.EventTarget
import nz.net.ultraq.redhorizon.engine.Component
import nz.net.ultraq.redhorizon.graphics.imgui.ImGuiContext
import nz.net.ultraq.redhorizon.graphics.imgui.ImGuiModule

/**
 * A component for adding UI using ImGui to the scene.
 *
 * @author Emanuel Rabina
 */
class ImGuiComponent implements Component<ImGuiComponent>, EventTarget<ImGuiComponent> {

	final ImGuiModule imGuiModule

	/**
	 * Constructor, save the ImGui module and relay any events it fires so they
	 * can be captured by scripts.
	 */
	ImGuiComponent(ImGuiModule imGuiModule) {

		this.imGuiModule = imGuiModule
		if (imGuiModule instanceof EventTarget) {
			imGuiModule.relay(Event, this)
		}
	}

	/**
	 * Render the UI component.
	 */
	void render(ImGuiContext context) {

		imGuiModule.render(context)
	}
}
