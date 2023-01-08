/* 
 * Copyright 2021, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import nz.net.ultraq.preferences.Preferences
import nz.net.ultraq.redhorizon.classic.filetypes.MixFile
import nz.net.ultraq.redhorizon.engine.Application
import nz.net.ultraq.redhorizon.engine.audio.AudioConfiguration
import nz.net.ultraq.redhorizon.engine.geometry.Dimension
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.WindowMaximizedEvent
import nz.net.ultraq.redhorizon.engine.input.KeyEvent
import nz.net.ultraq.redhorizon.engine.media.AnimationLoader
import nz.net.ultraq.redhorizon.engine.media.ImageLoader
import nz.net.ultraq.redhorizon.engine.media.ImagesLoader
import nz.net.ultraq.redhorizon.engine.media.MediaLoader
import nz.net.ultraq.redhorizon.engine.media.Playable
import nz.net.ultraq.redhorizon.engine.media.SoundLoader
import nz.net.ultraq.redhorizon.engine.media.VideoLoader
import nz.net.ultraq.redhorizon.filetypes.AnimationFile
import nz.net.ultraq.redhorizon.filetypes.ImageFile
import nz.net.ultraq.redhorizon.filetypes.ImagesFile
import nz.net.ultraq.redhorizon.filetypes.Palette
import nz.net.ultraq.redhorizon.filetypes.SoundFile
import nz.net.ultraq.redhorizon.filetypes.VideoFile

import org.joml.Vector3f
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.*

import java.util.concurrent.CopyOnWriteArrayList

/**
 * A Command & Conquer asset explorer, allows peeking into and previewing the
 * classic C&C files using a file explorer-like interface.
 *
 * @author Emanuel Rabina
 */
class Explorer extends Application {

	private static final Logger logger = LoggerFactory.getLogger(Explorer)
	private static final Preferences userPreferences = new Preferences()
	private static final Dimension renderResolution = new Dimension(1280, 800)

	private final List<Entry> entries = new CopyOnWriteArrayList<>()
	private final EntryList entryList
	private final MixDatabase mixDatabase

	private File currentDirectory
	private InputStream selectedFileInputStream
	private Object selectedFile
	private MediaLoader selectedLoader
	private Palette palette

	/**
	 * Constructor, sets up an application with the default configurations.
	 *
	 * @param version
	 * @param palette
	 */
	Explorer(String version, Palette palette) {

		super("Explorer - ${version}",
			new AudioConfiguration(),
			new GraphicsConfiguration(
				maximized: userPreferences.get(ExplorerPreferences.WINDOW_MAXIMIZED),
				renderResolution: renderResolution,
				startWithChrome: true
			)
		)

		entryList = new EntryList(entries)
		mixDatabase = new MixDatabase()

		buildList(new File(System.getProperty("user.dir")))

		// TODO: Be able to choose which palette to apply to a paletted file
		this.palette = palette
	}

	@Override
	protected void applicationStart() {

		// Include the explorer GUI in the render pipeline
		inputEventStream.on(KeyEvent) { keyEvent ->
			if (keyEvent.action == GLFW_PRESS) {
				if (keyEvent.key == GLFW_KEY_O) {
					entryList.enabled = !entryList.enabled
				}
			}
		}
		graphicsSystem.renderPipeline.addOverlayPass(entryList)

		graphicsSystem.on(WindowMaximizedEvent) { event ->
			userPreferences.set(ExplorerPreferences.WINDOW_MAXIMIZED, event.maximized)
		}

		// Handle events from the explorer GUI
		entryList.on(EntrySelectedEvent) { event ->
			clearPreview()
			def entry = event.entry
			if (entry instanceof MixEntry) {
				if (entry.name == '..') {
					buildList(currentDirectory)
				}
				else {
					previewEntry(entry)
				}
			}
			else if (entry instanceof FileEntry) {
				def file = entry.file
				if (file.directory) {
					buildList(file)
				}
				else if (file.name.endsWith('.mix')) {
					buildList(new MixFile(file))
				}
				else {
					previewFile(file)
				}
			}
		}

		// Universal quit on exit
		inputEventStream.on(KeyEvent) { event ->
			if (event.action == GLFW_PRESS && event.key == GLFW_KEY_ESCAPE) {
				stop()
			}
		}
	}

	@Override
	protected void applicationStop() {

		clearCurrentFile()
	}

	/**
	 * Update the contents of the list from the current directory.
	 *
	 * @param directory
	 */
	private void buildList(File directory) {

		entries.clear()

		if (directory.parent) {
			entries << new FileEntry(directory.parentFile, '/..')
		}
		directory.listFiles()
			.sort { file1, file2 ->
				file1.directory && !file2.directory ? -1 :
					!file1.directory && file2.directory ? 1 :
						file1.name <=> file2.name
			}
			.each { fileOrDirectory ->
				entries << new FileEntry(fileOrDirectory)
			}

		currentDirectory = directory
	}

	/**
	 * Update the contents of the list from the current mix file.
	 *
	 * @param mixFile
	 */
	private void buildList(MixFile mixFile) {

		entries.clear()
		entries << new MixEntry(mixFile, null, '..')

		def mixEntryTester = new MixEntryTester(mixFile)
		mixFile.entries.each { entry ->
//			println("0x${Integer.toHexString(entry.id)}")

			// Perform a lookup to see if we know about this file already, getting both a name and class
			def matchingData = mixDatabase.find(entry.id)
			if (matchingData) {
				entries << new MixEntry(mixFile, entry, matchingData.name, matchingData.name.fileClass)
			}

			// Otherwise try determine what kind of file this is, getting only a class
			else {
				def testerResult = mixEntryTester.test(entry)
				if (testerResult) {
					entries << new MixEntry(mixFile, entry, testerResult.name, null, testerResult.file, true)
				}
				else {
					entries << new MixEntry(mixFile, entry, "(unknown entry, ID: 0x${Integer.toHexString(entry.id)})", null, null, true)
				}
			}
		}

		entries.sort()
	}

	/**
	 * Finish all tasks associated with the current file so that we can exit the
	 * application or make way for another file.
	 */
	private void clearCurrentFile() {

		selectedLoader?.unload()
	}

	/**
	 * Clear the current entry in preview and reset the preview scene.
	 */
	private void clearPreview() {

		clearCurrentFile()

		// TODO: Wait for stop event before proceeding.  Need to allow the engines
		//       to clean up the item before clearing the scene
		Thread.sleep(100)

		scene.clear()
		graphicsSystem.camera.center(new Vector3f())
	}

	/**
	 * Update the preview are for the given file data and type.
	 *
	 * @param file
	 */
	private void preview(Object file) {

		selectedFile = file

		selectedLoader = switch (file) {
			case VideoFile -> new VideoLoader(file, scene, graphicsSystem, gameClock, inputEventStream)
			case AnimationFile -> new AnimationLoader(file, scene, graphicsSystem, gameClock, inputEventStream)
			case ImageFile -> new ImageLoader(file, scene, graphicsSystem)
			case ImagesFile -> new ImagesLoader(file, palette, scene, graphicsSystem, inputEventStream)
			case SoundFile -> new SoundLoader(file, scene, gameClock, inputEventStream)
			default -> logger.info('Filetype of {} not yet configured', selectedFile.class.simpleName)
		}

		var media = selectedLoader.load()
		if (media instanceof Playable) {
			media.play()
		}

		// TODO: Display selected file info elsewhere?  Currently these are long
		//       lines of text 🤔
//		logger.info('{}', selectedFileClass)
	}

	/**
	 * Update the preview area with the media for the selected mix file entry.
	 *
	 * @param entry
	 */
	private void previewEntry(MixEntry entry) {

		logger.info('Loading {} from mix file', entry.name ?: '(unknown)')

		def file = entry.file
		def fileClass = entry.fileClass

		if (file) {
			selectedFileInputStream = entry.mixFile.getEntryData(entry.mixEntry)
			preview(file)
		}
		else if (fileClass) {
			selectedFileInputStream = entry.mixFile.getEntryData(entry.mixEntry)
			preview(fileClass.newInstance(selectedFileInputStream))
		}
		else {
			logger.info('No filetype implementation for {}', entry.name ?: '(unknown)')
		}
	}

	/**
	 * Update the preview area with the media for the selected file.
	 *
	 * @param file
	 */
	private void previewFile(File file) {

		logger.info('Loading {}...', file.name)

		def fileClass = file.name.fileClass
		if (fileClass) {
			selectedFileInputStream = file.newInputStream()
			preview(fileClass.newInstance(selectedFileInputStream))
		}
		else {
			logger.info('No filetype implementation for {}', file.name)
		}
	}
}
