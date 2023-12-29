/*
 * Copyright 2022, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.explorer

import nz.net.ultraq.redhorizon.engine.graphics.Framebuffer
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.pipeline.OverlayRenderPass
import nz.net.ultraq.redhorizon.events.EventTarget

import imgui.ImGui
import imgui.type.ImBoolean
import static imgui.flag.ImGuiCond.FirstUseEver
import static imgui.flag.ImGuiStyleVar.WindowPadding

/**
 * Renders the file/entry list window for the explorer application.
 *
 * @author Emanuel Rabina
 */
class EntryList implements EventTarget, OverlayRenderPass {

	final List<Entry> entries

	private Entry selectedEntry

	EntryList(List<Entry> entries) {

		this.entries = entries
		this.enabled = true
	}

	@Override
	void delete(GraphicsRenderer renderer) {
	}

	@Override
	void render(GraphicsRenderer renderer, Framebuffer sceneResult) {

		ImGui.setNextWindowSize(300, 500, FirstUseEver)
		ImGui.pushStyleVar(WindowPadding, 0, 0)
		ImGui.begin('Current directory', new ImBoolean(true))
		ImGui.popStyleVar()

		// File list
		if (ImGui.beginListBox('##FileList', -Float.MIN_VALUE, -Float.MIN_VALUE)) {
			entries.each { entry ->
				// noinspection ChangeToOperator
				def isSelected = selectedEntry.equals(entry)
				if (ImGui.selectable(entry.name, isSelected)) {
					selectedEntry = entry
					trigger(new EntrySelectedEvent(entry))
				}
				if (isSelected) {
					ImGui.setItemDefaultFocus()
				}
			}
			ImGui.endListBox()
		}

		ImGui.end()
	}
}
