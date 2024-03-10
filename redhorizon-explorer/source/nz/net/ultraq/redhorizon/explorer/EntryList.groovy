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
import nz.net.ultraq.redhorizon.engine.graphics.imgui.ImGuiLayer
import nz.net.ultraq.redhorizon.engine.graphics.pipeline.ImGuiElement
import nz.net.ultraq.redhorizon.events.EventTarget

import imgui.ImGui
import imgui.type.ImBoolean
import static imgui.flag.ImGuiCond.FirstUseEver
import static imgui.flag.ImGuiSelectableFlags.SpanAllColumns
import static imgui.flag.ImGuiStyleVar.WindowPadding
import static imgui.flag.ImGuiTableFlags.*

import java.text.DecimalFormat

/**
 * Renders the file/entry list window for the explorer application.
 *
 * @author Emanuel Rabina
 */
class EntryList implements EventTarget, ImGuiElement {

	private static final DecimalFormat numberFormat = new DecimalFormat('#,###,##0')

	final List<Entry> entries

	private Entry selectedEntry

	EntryList(List<Entry> entries) {

		this.entries = entries
		this.enabled = true
	}

	@Override
	void render(int dockspaceId, Framebuffer sceneResult) {

		ImGui.setNextWindowSize(300, 500, FirstUseEver)
		ImGui.pushStyleVar(WindowPadding, 0, 0)
		ImGui.begin('Current directory', new ImBoolean(true))
		ImGui.popStyleVar()

		// File list
		if (ImGui.beginTable('FileTable', 3, BordersV | Resizable | RowBg | ScrollY)) {
			ImGui.tableSetupScrollFreeze(0, 1)
			ImGui.tableSetupColumn('Name')
			ImGui.tableSetupColumn('Type')
			ImGui.tableSetupColumn('Size')
			ImGui.tableHeadersRow()

			entries.each { entry ->
				ImGui.tableNextRow()

				ImGui.tableSetColumnIndex(0)
				// noinspection ChangeToOperator
				var isSelected = selectedEntry.equals(entry)
				if (ImGui.selectable(entry.name, isSelected, SpanAllColumns)) {
					selectedEntry = entry
					trigger(new EntrySelectedEvent(entry))
				}
				if (isSelected) {
					ImGui.setItemDefaultFocus()
				}

				ImGui.tableSetColumnIndex(1)
				ImGui.text(entry.type ?: '')

				ImGui.tableSetColumnIndex(2)
				ImGui.pushFont(ImGuiLayer.robotoMonoFont)
				ImGui.text(String.format('%12s', numberFormat.format(entry.size)))
				ImGui.popFont()
			}

			ImGui.endTable()
		}

		ImGui.end()
	}
}
