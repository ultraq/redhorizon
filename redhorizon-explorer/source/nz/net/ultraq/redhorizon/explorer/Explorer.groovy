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
import nz.net.ultraq.redhorizon.Application
import nz.net.ultraq.redhorizon.classic.filetypes.mix.MixFile
import nz.net.ultraq.redhorizon.engine.EngineLoopStartEvent
import nz.net.ultraq.redhorizon.engine.audio.AudioConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.WindowMaximizedEvent
import nz.net.ultraq.redhorizon.engine.input.KeyEvent
import nz.net.ultraq.redhorizon.filetypes.AnimationFile
import nz.net.ultraq.redhorizon.filetypes.ImageFile
import nz.net.ultraq.redhorizon.filetypes.ImagesFile
import nz.net.ultraq.redhorizon.filetypes.Palette
import nz.net.ultraq.redhorizon.filetypes.SoundFile
import nz.net.ultraq.redhorizon.filetypes.Streaming
import nz.net.ultraq.redhorizon.filetypes.VideoFile
import nz.net.ultraq.redhorizon.geometry.Dimension
import nz.net.ultraq.redhorizon.media.AnimationLoader
import nz.net.ultraq.redhorizon.media.Image
import nz.net.ultraq.redhorizon.media.ImageStrip
import nz.net.ultraq.redhorizon.media.Playable
import nz.net.ultraq.redhorizon.media.SoundLoader
import nz.net.ultraq.redhorizon.media.VideoLoader

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
	private static final Dimension renderResolution = new Dimension(640, 400)

	private final List<Entry> entries = new CopyOnWriteArrayList<>()
	private final EntryList entryList
	private final MixDatabase mixDatabase

	private File currentDirectory
	private InputStream selectedFileInputStream
	private Object selectedFileClass
	private Object selectedMedia
	private Palette palette
	private int tick

	/**
	 * Constructor, sets up an application with the default configurations.
	 * 
	 * @param version
	 * @param palette
	 */
	Explorer(String version, Palette palette) {

		super("Red Horizon Explorer ${version}",
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
					entries << new MixEntry(mixFile, entry, testerResult.name, testerResult.fileClass, true)
				}
				else {
					entries << new MixEntry(mixFile, entry, "(unknown entry, ID: 0x${Integer.toHexString(entry.id)})", null, true)
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

		if (selectedMedia) {
			if (selectedFileClass instanceof Streaming) {
				logger.debug('Stopping streaming worker')
				selectedFileClass.streamingDataWorker.stop()
			}
			if (selectedMedia instanceof Playable) {
				logger.debug('Stopping playable file')
				selectedMedia.stop()
			}
			selectedFileInputStream.close()

			// TODO: Wait for stop event before proceeding.  Need to allow the engines
			//       to clean up the item before clearing the scene
			Thread.sleep(100)
		}
	}

	/**
	 * Clear the current entry in preview and reset the preview scene.
	 */
	private void clearPreview() {

		clearCurrentFile()
		scene.clear()
		graphicsEngine.camera.center(new Vector3f())
	}

	/**
	 * Update the preview are for the given file data and type.
	 * 
	 * @param fileType
	 */
	private void preview(Object fileType) {

		switch (fileType) {

			case VideoFile:
				def videoLoader = new VideoLoader(scene, graphicsEngine, inputEventStream, gameClock)
				selectedMedia = videoLoader.load(fileType)
				selectedMedia.play()
				break

			case AnimationFile:
				def animationLoader = new AnimationLoader(scene, graphicsEngine, inputEventStream, gameClock)
				selectedMedia = animationLoader.load(fileType)
				selectedMedia.play()
				break

			case ImageFile:
				def image = new Image(fileType)
				scene << image
					.translate(-fileType.width / 2, -fileType.height / 2)
				selectedMedia = image
				break

			case ImagesFile:
				tick = fileType.width
				def imageStrip = new ImageStrip(fileType, palette)
				scene << imageStrip
				selectedMedia = imageStrip
				break

			case SoundFile:
				def soundLoader = new SoundLoader(scene, inputEventStream, gameClock)
				selectedMedia = soundLoader.load(fileType)
				selectedMedia.play()
				break

			default:
				logger.info('Filetype of {} not yet configured', selectedFileClass.class.simpleName)
				selectedMedia = null
		}

		selectedFileClass = fileType
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

		clearPreview()
		logger.info('Loading {} from mix file', entry.name ?: '(unknown)')

		def entryClass = entry.fileClass
		if (!entryClass) {
			logger.info('No filetype implementation for {}', entry.name ?: '(unknown)')
			return
		}

		selectedFileInputStream = entry.mixFile.getEntryData(entry.mixEntry)
		preview(entryClass.newInstance(selectedFileInputStream))
	}

	/**
	 * Update the preview area with the media for the selected file.
	 * 
	 * @param file
	 */
	private void previewFile(File file) {

		clearPreview()
		logger.info('Loading {}...', file.name)

		def fileClass = file.name.fileClass
		if (!fileClass) {
			logger.info('No filetype implementation for {}', file.name)
			return
		}

		selectedFileInputStream = file.newInputStream()
		preview(fileClass.newInstance(selectedFileInputStream))
	}

	@Override
	void run() {

		// Include the explorer GUI in the render pipeline
		graphicsEngine.on(EngineLoopStartEvent) { event ->
			inputEventStream.on(KeyEvent) { keyEvent ->
				if (keyEvent.action == GLFW_PRESS) {
					if (keyEvent.key == GLFW_KEY_O) {
						entryList.enabled = !entryList.enabled
					}
				}
			}
			graphicsEngine.renderPipeline.addOverlayPass(entryList)
		}
		graphicsEngine.on(WindowMaximizedEvent) { event ->
			userPreferences.set(ExplorerPreferences.WINDOW_MAXIMIZED, event.maximized)
		}

		// Handle events from the explorer GUI
		entryList.on(EntrySelectedEvent) { event ->
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

		// Key event handler
		inputEventStream.on(KeyEvent) { event ->
			if (event.action == GLFW_PRESS || event.action == GLFW_REPEAT) {
				switch (event.key) {
					case GLFW_KEY_LEFT:
						graphicsEngine.camera.translate(tick, 0)
						break
					case GLFW_KEY_RIGHT:
						graphicsEngine.camera.translate(-tick, 0)
						break
					case GLFW_KEY_SPACE:
						if (selectedFileClass instanceof AnimationFile) {
							gameClock.togglePause()
						}
						else if (selectedFileClass instanceof ImagesFile) {
							graphicsEngine.camera.center(new Vector3f())
						}
						break
					case GLFW_KEY_ESCAPE:
						stop()
						break
				}
			}
		}

		// Cleanup on exit
		on(ApplicationStoppingEvent) { event ->
			clearCurrentFile()
		}
	}
}
