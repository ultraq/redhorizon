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

package nz.net.ultraq.redhorizon.explorer.objects

import nz.net.ultraq.eventhorizon.Event
import nz.net.ultraq.eventhorizon.EventTarget
import nz.net.ultraq.redhorizon.classic.filetypes.MixFile
import nz.net.ultraq.redhorizon.classic.filetypes.RaMixDatabase
import nz.net.ultraq.redhorizon.engine.Entity
import nz.net.ultraq.redhorizon.engine.graphics.imgui.ImGuiComponent
import nz.net.ultraq.redhorizon.engine.graphics.imgui.LogPanel
import nz.net.ultraq.redhorizon.engine.graphics.imgui.NodeList
import nz.net.ultraq.redhorizon.explorer.Entry
import nz.net.ultraq.redhorizon.explorer.ExplorerScene
import nz.net.ultraq.redhorizon.explorer.FileEntry
import nz.net.ultraq.redhorizon.explorer.mixdata.MixEntry
import nz.net.ultraq.redhorizon.explorer.mixdata.MixEntryTester
import nz.net.ultraq.redhorizon.explorer.ui.EntryList
import nz.net.ultraq.redhorizon.explorer.ui.EntrySelectedEvent
import nz.net.ultraq.redhorizon.explorer.ui.MainMenuBar
import nz.net.ultraq.redhorizon.graphics.Window
import nz.net.ultraq.redhorizon.graphics.imgui.DebugOverlay

import java.util.concurrent.CopyOnWriteArrayList

/**
 * Manage UI elements in the explorer.
 *
 * @author Emanuel Rabina
 */
class UiController extends Entity<UiController> implements EventTarget<UiController> {

	private File currentDirectory
	private final List<Entry> entries = new CopyOnWriteArrayList<>()

	/**
	 * Constructor, build and register the many UI pieces.
	 */
	UiController(ExplorerScene scene, Window window, boolean touchpadInput, File startingDirectory) {

		var mainMenuBar = new MainMenuBar(touchpadInput)
			.relay(Event, this)
		var entryList = new EntryList(entries)

		addComponent(new ImGuiComponent(new DebugOverlay()
			.withCursorTracking(window, scene.camera)
			.withPersistentLogging()))
		addComponent(new ImGuiComponent(mainMenuBar))
		addComponent(new ImGuiComponent(new NodeList(scene)))
		addComponent(new ImGuiComponent(entryList))
		addComponent(new ImGuiComponent(new LogPanel()))

		entryList.on(EntrySelectedEvent) { event ->
			var entry = event.entry()
			if (entry instanceof MixEntry) {
				if (entry.name == '..') {
					buildList(currentDirectory)
				}
				else {
					trigger(event)
				}
			}
			else if (entry instanceof FileEntry) {
				var file = entry.file
				if (file.directory) {
					buildList(file)
				}
				else if (file.name.endsWith('.mix')) {
					buildList(new MixFile(file))
				}
				trigger(event)
			}
		}

		buildList(startingDirectory)
	}

	/**
	 * Update the contents of the list from the current directory.
	 */
	private void buildList(File directory) {

		entries.clear()

		if (directory.parent) {
			entries << new FileEntry(
				file: directory.parentFile,
				name: '/..'
			)
		}
		directory
			.listFiles()
			.sort { file1, file2 ->
				file1.directory && !file2.directory ? -1 :
					!file1.directory && file2.directory ? 1 :
						file1.name <=> file2.name
			}
			.each { fileOrDirectory ->
				entries << new FileEntry(
					file: fileOrDirectory,
					type: fileOrDirectory.supportedFileName
				)
			}

		currentDirectory = directory
	}

	/**
	 * Update the contents of the list from the current mix file.
	 */
	private void buildList(MixFile mixFile) {

		entries.clear()
		entries << new MixEntry(mixFile, null, '..')

		// RA-MIXer built-in database, if available
		var raMixDbEntry = mixFile.getEntry(0x7fffffff)
		var raMixDb = raMixDbEntry ? new RaMixDatabase(mixFile.getEntryData(raMixDbEntry)) : null

		// TODO: Also support XCC local database

		var mixEntryTester = new MixEntryTester(mixFile)
		mixFile.entries.each { entry ->

			if (raMixDb) {
				if (entry.id == 0x7fffffff) {
					entries << new MixEntry(mixFile, entry, 'RA-MIXer localDB', null, entry.size, false, 'Local database created by the RA-MIXer tool')
					return
				}

				var dbEntry = raMixDb.entries.find { dbEntry -> dbEntry.id() == entry.id }
				if (dbEntry) {
					entries << new MixEntry(mixFile, entry, dbEntry.name(), dbEntry.name().fileClass, entry.size, false, dbEntry.description())
					return
				}
			}

			// Perform a lookup to see if we know about this file already, getting both a name and class
			var dbEntry = mixDatabase.find(entry.id)
			if (dbEntry) {
				entries << new MixEntry(mixFile, entry, dbEntry.name(), dbEntry.name().fileClass, entry.size)
				return
			}

			// Otherwise try determine what kind of file this is, getting only a class
			var testerResult = mixEntryTester.test(entry)
			if (testerResult) {
				entries << new MixEntry(mixFile, entry, testerResult.name, testerResult.fileClass, entry.size, true)
			}
			else {
				entries << new MixEntry(mixFile, entry, "(unknown entry, ID: 0x${Integer.toHexString(entry.id)})", null, entry.size, true)
			}
		}

		entries.sort()
	}
}
