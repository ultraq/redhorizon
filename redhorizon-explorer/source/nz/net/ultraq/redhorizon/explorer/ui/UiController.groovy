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

import nz.net.ultraq.eventhorizon.Event
import nz.net.ultraq.eventhorizon.EventTarget
import nz.net.ultraq.redhorizon.classic.filetypes.MixFile
import nz.net.ultraq.redhorizon.engine.Entity
import nz.net.ultraq.redhorizon.engine.graphics.imgui.ImGuiComponent
import nz.net.ultraq.redhorizon.engine.graphics.imgui.LogPanel
import nz.net.ultraq.redhorizon.engine.graphics.imgui.NodeList
import nz.net.ultraq.redhorizon.engine.scripts.EntityScript
import nz.net.ultraq.redhorizon.engine.scripts.ScriptComponent
import nz.net.ultraq.redhorizon.explorer.ExplorerScene
import nz.net.ultraq.redhorizon.explorer.filedata.FileEntry
import nz.net.ultraq.redhorizon.explorer.mixdata.MixDatabase
import nz.net.ultraq.redhorizon.explorer.mixdata.MixEntry
import nz.net.ultraq.redhorizon.explorer.mixdata.MixEntryTester
import nz.net.ultraq.redhorizon.explorer.mixdata.RaMixDatabase
import nz.net.ultraq.redhorizon.graphics.Window
import nz.net.ultraq.redhorizon.graphics.imgui.DebugOverlay

import static org.lwjgl.glfw.GLFW.*

import java.util.concurrent.CopyOnWriteArrayList

/**
 * Manage UI elements in the explorer.
 *
 * @author Emanuel Rabina
 */
class UiController extends Entity<UiController> implements EventTarget<UiController> {

	private final File startingDirectory
	private final MixDatabase mixDatabase
	private final MainMenuBar mainMenuBar
	private final EntryList entryList
	private final List<Entry> entries = new CopyOnWriteArrayList<>()

	/**
	 * Constructor, build and register the many UI pieces.
	 */
	UiController(ExplorerScene scene, Window window, boolean touchpadInput, File startingDirectory,
		MixDatabase mixDatabase) {

		this.startingDirectory = startingDirectory
		this.mixDatabase = mixDatabase

		mainMenuBar = new MainMenuBar(touchpadInput)
			.relay(Event, scene)
		entryList = new EntryList(entries)

		addComponent(new ImGuiComponent(new DebugOverlay()
			.withCursorTracking(window, scene.camera)
			.withProfilingLogging()))
		addComponent(new ImGuiComponent(mainMenuBar))
		addComponent(new ImGuiComponent(new NodeList(scene)))
		addComponent(new ImGuiComponent(entryList))
		addComponent(new ImGuiComponent(new LogPanel()))
		addComponent(new ScriptComponent(UiControllerScript))
	}

	static class UiControllerScript extends EntityScript<UiController> {

		private ExplorerScene scene
		private File currentDirectory

		/**
		 * Update the contents of the list from the current directory.
		 */
		private void buildList(File directory) {

			var entries = entity.entries
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
						type: fileOrDirectory.supportedFileType
					)
				}

			currentDirectory = directory
		}

		/**
		 * Update the contents of the list from the current mix file.
		 */
		private void buildList(MixFile mixFile) {

			var entries = entity.entries
			entries.clear()
			entries << new MixEntry(mixFile, null, '..')

			// RA-MIXer built-in database, if available
			var raMixDbEntry = mixFile.getEntry(0x7fffffff)
			var raMixDb = raMixDbEntry ? new RaMixDatabase(mixFile.getEntryData(raMixDbEntry)) : null

			// TODO: Also support XCC local database

			mixFile.entries.each { entry ->

				if (raMixDb) {
					if (entry.id == 0x7fffffff) {
						entries << new MixEntry(mixFile, entry, 'RA-MIXer localDB', null, null, entry.size, false, 'Local database created by the RA-MIXer tool')
						return
					}

					var dbEntry = raMixDb.entries.find { dbEntry -> dbEntry.id() == entry.id }
					if (dbEntry) {
						entries << new MixEntry(mixFile, entry, dbEntry.name(), dbEntry.supportedFileType,
							dbEntry.supportedFileClass, entry.size, false, dbEntry.description())
						return
					}
				}

				// Perform a lookup to see if we know about this file already, getting both a name and class
				var dbEntry = entity.mixDatabase.find(entry.id)
				if (dbEntry) {
					entries << new MixEntry(mixFile, entry, dbEntry.name(), dbEntry.supportedFileType, dbEntry.supportedFileClass,
						entry.size)
					return
				}

				// Otherwise try determine what kind of file this is, getting only a class
				var testResult = new MixEntryTester(mixFile).test(entry)
				if (testResult) {
					entries << new MixEntry(mixFile, entry, testResult.name(), testResult.type(), testResult.fileClass(),
						entry.size, true)
				}
				else {
					entries << new MixEntry(mixFile, entry, "(unknown entry, ID: 0x${Integer.toHexString(entry.id)})", null, null,
						entry.size, true)
				}
			}

			entries.sort()
		}

		@Override
		void init() {

			scene = entity.scene as ExplorerScene
			entity.entryList.on(EntrySelectedEvent) { event ->
				var entry = event.entry()
				if (entry instanceof MixEntry) {
					if (entry.name == '..') {
						buildList(currentDirectory)
					}
					else {
						scene.trigger(event)
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
					scene.trigger(event)
				}
			}

			buildList(entity.startingDirectory)
		}

		@Override
		void update(float delta) {

			if (input.keyPressed(GLFW_KEY_UP, true) && entity.entryList.focused) {
				entity.entryList.selectPrevious()
			}
			else if (input.keyPressed(GLFW_KEY_DOWN, true) && entity.entryList.focused) {
				entity.entryList.selectNext()
			}
		}
	}
}
