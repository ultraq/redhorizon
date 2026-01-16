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

package nz.net.ultraq.redhorizon.explorer.ui

import nz.net.ultraq.eventhorizon.EventTarget
import nz.net.ultraq.redhorizon.explorer.filedata.FileEntry
import nz.net.ultraq.redhorizon.explorer.mixdata.MixEntry
import nz.net.ultraq.redhorizon.explorer.ui.actions.ExtractMixFileEntry
import nz.net.ultraq.redhorizon.graphics.imgui.ImGuiContext
import nz.net.ultraq.redhorizon.graphics.imgui.ImGuiModule

import imgui.ImGui
import imgui.flag.ImGuiSortDirection
import imgui.type.ImBoolean
import static imgui.flag.ImGuiCond.FirstUseEver
import static imgui.flag.ImGuiFocusedFlags.ChildWindows
import static imgui.flag.ImGuiSelectableFlags.SpanAllColumns
import static imgui.flag.ImGuiStyleVar.WindowPadding
import static imgui.flag.ImGuiTableFlags.*

import groovy.transform.TupleConstructor

/**
 * Renders the file/entry list window for the explorer application.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false, includes = 'entries')
class EntryList implements EventTarget<EntryList>, ImGuiModule {

	final List<Entry> entries
	private boolean focused
	private boolean hovered
	private Entry selectedEntry
	private boolean selectedEntryTriggered
	private boolean entryVisibleOnce

	@Override
	void render(ImGuiContext context) {

		ImGui.setNextWindowSize(300, 500, FirstUseEver)
		ImGui.pushStyleVar(WindowPadding, 0, 0)
		ImGui.begin('Current directory', new ImBoolean(true))
		ImGui.popStyleVar()

		focused = ImGui.isWindowFocused(ChildWindows)
		hovered = ImGui.isWindowHovered()

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
				var specialEntry = null
				var specialEntryIndex = entries.findIndexOf { e -> e.name() == '/..' || e.name() == '..' }
				if (specialEntryIndex != -1) {
					specialEntry = entries.remove(specialEntryIndex)
				}

				var sortingColumnSpecs = tableSortSpecs.specs[0]
				switch (sortingColumnSpecs.columnIndex) {
					case 1 -> entries.sort { it.type() }
					case 2 -> entries.sort { it.size() }
					case 3 && entries[0] instanceof MixEntry -> entries.sort { it.description() }
					default -> entries.sort { it.name() }
				}
				if (sortingColumnSpecs.sortDirection == ImGuiSortDirection.Descending) {
					entries.reverse(true)
				}

				if (specialEntry) {
					entries.add(0, specialEntry)
				}

				tableSortSpecs.specsDirty = false
			}

			entries.each { entry ->
				ImGui.tableNextRow()

				ImGui.tableSetColumnIndex(0)
				// noinspection ChangeToOperator
				var isSelected = selectedEntry.equals(entry)
				if (ImGui.selectable(entry.name(), isSelected, SpanAllColumns)) {
					updateSelection(entry)
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
				if (entry instanceof MixEntry) {
					if (ImGui.beginPopupContextItem()) {
						if (ImGui.selectable('Extract')) {
							new ExtractMixFileEntry().extract(entry, entry.name()) // TODO: Save to specified location
							ImGui.closeCurrentPopup()
						}
						ImGui.endPopup()
					}
				}

				ImGui.tableSetColumnIndex(1)
				ImGui.text(entry.type() ?: '')

				ImGui.tableSetColumnIndex(2)
				if (!(entry instanceof FileEntry && entry.file().directory)) {
					ImGui.pushFont(context.monospaceFont)
					ImGui.text(sprintf('%,12d', entry.size()))
					ImGui.popFont()
				}

				if (entry instanceof MixEntry && entry.description()) {
					ImGui.tableSetColumnIndex(3)
					ImGui.text(entry.description())
				}
			}

			ImGui.endTable()
		}

		ImGui.end()
	}

	/**
	 * Return whether this window is focused.
	 */
	boolean isFocused() {

		return focused
	}

	/**
	 * Return whether this window is hovered.
	 */
	boolean isHovered() {

		return hovered
	}

	/**
	 * Select the next entry in the current list.
	 */
	void selectNext() {

		var currentIndex = entries.indexOf(selectedEntry)
		if (currentIndex < entries.size() - 1) {
			updateSelection(entries[currentIndex + 1])
		}
	}

	/**
	 * Select the previous entry in the current list.
	 */
	void selectPrevious() {

		var currentIndex = entries.indexOf(selectedEntry)
		if (currentIndex > 1) {
			updateSelection(entries[currentIndex - 1])
		}
	}

	/**
	 * Update which is the selected entry.
	 */
	private void updateSelection(Entry entry) {

		selectedEntry = entry
		selectedEntryTriggered = false
		entryVisibleOnce = false
	}
}
