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
import nz.net.ultraq.redhorizon.classic.filetypes.MixFile
import nz.net.ultraq.redhorizon.engine.graphics.imgui.ImGuiComponent
import nz.net.ultraq.redhorizon.engine.scripts.EntityScript
import nz.net.ultraq.redhorizon.explorer.filedata.FileEntry
import nz.net.ultraq.redhorizon.explorer.filedata.FileTester
import nz.net.ultraq.redhorizon.explorer.mixdata.MixDatabase
import nz.net.ultraq.redhorizon.explorer.mixdata.MixEntry
import nz.net.ultraq.redhorizon.explorer.mixdata.RaMixDatabase
import nz.net.ultraq.redhorizon.explorer.previews.PreviewController
import nz.net.ultraq.redhorizon.explorer.ui.actions.ExtractMixFileEntryAction
import nz.net.ultraq.redhorizon.explorer.ui.actions.SelectEntryAction
import nz.net.ultraq.redhorizon.explorer.ui.actions.ToggleTouchpadInputAction

import static org.lwjgl.glfw.GLFW.*

/**
 * Manage UI elements in the explorer.
 *
 * @author Emanuel Rabina
 */
class UiController extends EntityScript implements EventTarget<UiController> {

	private UiSettingsComponent uiSettings
	private MixDatabase mixDatabase
	private EntryList entryList
	private List<Entry> entries
	private File currentDirectory

	/**
	 * Update the contents of the list from the current directory.
	 */
	void buildList(File directory) {

		entries.clear()

		if (directory.parent) {
			entries << new FileEntry(directory.parentFile, '/..', null)
		}
		directory
			.listFiles()
			.sort { file1, file2 ->
				file1.directory && !file2.directory ? -1 :
					!file1.directory && file2.directory ? 1 :
						file1.name <=> file2.name
			}
			.each { fileOrDirectory ->
				entries << new FileEntry(fileOrDirectory, null, fileOrDirectory.guessedFileType())
			}

		currentDirectory = directory
	}

	/**
	 * Update the contents of the list from the current mix file.
	 */
	void buildList(MixFile mixFile) {

		entries.clear()
		entries << new MixEntry(currentDirectory, mixFile, null, '..', null, 0, null, false)

		// RA-MIXer built-in database, if available
		var raMixDbEntry = mixFile.getEntry(0x7fffffff)
		var raMixDb = raMixDbEntry ? new RaMixDatabase(mixFile.getEntryData(raMixDbEntry)) : null

		// TODO: Also support XCC local database

		mixFile.entries.each { entry ->

			if (raMixDb) {
				if (entry.id == 0x7fffffff) {
					entries << new MixEntry(currentDirectory, mixFile, entry, 'RA-MIXer localDB', null, entry.size,
						'Local database created by the RA-MIXer tool', false)
					return
				}

				var dbEntry = raMixDb.entries.find { dbEntry -> dbEntry.id() == entry.id }
				if (dbEntry) {
					entries << new MixEntry(currentDirectory, mixFile, entry, dbEntry.name(), dbEntry.guessedFileType(),
						entry.size, dbEntry.description(), false)
					return
				}
			}

			// Perform a lookup to see if we know about this file already, getting both a name and class
			var dbEntry = mixDatabase.find(entry.id)
			if (dbEntry) {
				entries << new MixEntry(currentDirectory, mixFile, entry, dbEntry.name(), dbEntry.guessedFileType(), entry.size,
					null, false)
				return
			}

			// Otherwise run some tests to see if we can determine what kind of file this is
			var testResult = new FileTester().test(null, entry.size, mixFile.getEntryData(entry))
			if (testResult) {
				entries << new MixEntry(currentDirectory, mixFile, entry, "0x${Integer.toHexString(entry.id)}",
					testResult.type(), entry.size,
					null, true)
			}
			else {
				entries << new MixEntry(currentDirectory, mixFile, entry, "0x${Integer.toHexString(entry.id)}", null,
					entry.size, null, true)
			}
		}

		entries.sort()
	}

	@Override
	void init() {

		uiSettings = entity.findComponentByType(UiSettingsComponent)
		mixDatabase = uiSettings.mixDatabase

		entryList = (entity.findComponent { it.name == 'Entry list' } as ImGuiComponent)?.imGuiModule as EntryList
		entryList
			.on(EntrySelectedEvent) { event ->
				new SelectEntryAction(this, scene.findScriptByType(PreviewController), event.entry()).select()
			}
			.on(ExtractMixEntryEvent) { event ->
				new ExtractMixFileEntryAction(event.entry(), event.entry().name()).extract() // TODO: Save to specified location
			}
		entries = entryList.entries

		var mainMenuBar = (entity.findComponent { it.name == 'Main menu' } as ImGuiComponent)
		mainMenuBar.on(TouchpadInputEvent) { event ->
			new ToggleTouchpadInputAction(scene, uiSettings).toggle()
		}

		buildList(uiSettings.startingDirectory)
	}

	@Override
	void update(float delta) {

		if (input.keyPressed(GLFW_KEY_UP, true) && entryList.focused) {
			entryList.selectPrevious()
		}
		else if (input.keyPressed(GLFW_KEY_DOWN, true) && entryList.focused) {
			entryList.selectNext()
		}
	}
}
