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
import nz.net.ultraq.redhorizon.engine.graphics.imgui.ImGuiElement
import nz.net.ultraq.redhorizon.engine.graphics.imgui.ImGuiLayer
import nz.net.ultraq.redhorizon.events.EventTarget

import imgui.ImGui
import imgui.flag.ImGuiSortDirection
import imgui.type.ImBoolean
import static imgui.flag.ImGuiCond.FirstUseEver
import static imgui.flag.ImGuiFocusedFlags.ChildWindows
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
	boolean focused

	private Entry selectedEntry
	private boolean selectedEntryTriggered
	private boolean entryVisibleOnce

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

		focused = ImGui.isWindowFocused(ChildWindows)

		// File list
		if (ImGui.beginTable('FileTable', 4, BordersV | Resizable | RowBg | ScrollX | ScrollY | Sortable)) {
			ImGui.tableSetupScrollFreeze(0, 1)
			ImGui.tableSetupColumn('Name')
			ImGui.tableSetupColumn('Type')
			ImGui.tableSetupColumn('Size')
			ImGui.tableSetupColumn('Description')
			ImGui.tableHeadersRow()

			var tableSortSpecs = ImGui.tableGetSortSpecs()
			if (tableSortSpecs.specsDirty) {

				// Take the special .. entry out to put back at the top after
				var specialEntry = entries.remove(entries.findIndexOf { e -> e.name == '/..' || e.name == '..' })

				var sortingColumn = tableSortSpecs.specs.columnIndex
				switch (sortingColumn) {
					case 1 -> entries.sort { it.type }
					case 2 -> entries.sort { it.size }
					case 3 && entries[0] instanceof MixEntry -> entries.sort { it.description }
					default -> entries.sort { it.name }
				}
				if (tableSortSpecs.specs.sortDirection == ImGuiSortDirection.Descending) {
					entries.reverse(true)
				}

				entries.add(0, specialEntry)

				tableSortSpecs.specsDirty = false
			}

			entries.each { entry ->
				ImGui.tableNextRow()

				ImGui.tableSetColumnIndex(0)
				// noinspection ChangeToOperator
				var isSelected = selectedEntry.equals(entry)
				if (ImGui.selectable(entry.name, isSelected, SpanAllColumns)) {
					selectedEntry = entry
					selectedEntryTriggered = false
					entryVisibleOnce = false
				}
				if (isSelected) {
					ImGui.setItemDefaultFocus()
					if (!selectedEntryTriggered) {
						trigger(new EntrySelectedEvent(entry))
						selectedEntryTriggered = true
					}
					if (!ImGui.isItemVisible() && !entryVisibleOnce) {
						ImGui.setScrollFromPosY(ImGui.getItemRectMinY())
						entryVisibleOnce = true
					}
				}

				ImGui.tableSetColumnIndex(1)
				ImGui.text(entry.type ?: '')

				ImGui.tableSetColumnIndex(2)
				ImGui.pushFont(ImGuiLayer.robotoMonoFont)
				ImGui.text(String.format('%12s', numberFormat.format(entry.size)))
				ImGui.popFont()

				if (entry instanceof MixEntry && entry.description) {
					ImGui.tableSetColumnIndex(3)
					ImGui.text(entry.description)
				}
			}

			ImGui.endTable()
		}

		ImGui.end()
	}

	/**
	 * Select the next entry in the current list.
	 */
	void selectNext() {

		if (focused) {
			var currentIndex = entries.indexOf(selectedEntry)
			if (currentIndex < entries.size() - 1) {
				selectedEntry = entries[currentIndex + 1]
				selectedEntryTriggered = false
				entryVisibleOnce = false
			}
		}
	}

	/**
	 * Select the previous entry in the current list.
	 */
	void selectPrevious() {

		if (focused) {
			var currentIndex = entries.indexOf(selectedEntry)
			if (currentIndex > 1) {
				selectedEntry = entries[currentIndex - 1]
				selectedEntryTriggered = false
				entryVisibleOnce = false
			}
		}
	}
}
