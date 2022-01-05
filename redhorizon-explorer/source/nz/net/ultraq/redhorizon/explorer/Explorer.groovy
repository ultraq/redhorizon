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
import nz.net.ultraq.redhorizon.classic.PaletteType
import nz.net.ultraq.redhorizon.classic.filetypes.aud.AudFile
import nz.net.ultraq.redhorizon.classic.filetypes.mix.MixEntry
import nz.net.ultraq.redhorizon.classic.filetypes.mix.MixFile
import nz.net.ultraq.redhorizon.classic.filetypes.pal.PalFile
import nz.net.ultraq.redhorizon.engine.EngineLoopStartEvent
import nz.net.ultraq.redhorizon.engine.audio.AudioConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.Framebuffer
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.OverlayRenderPass
import nz.net.ultraq.redhorizon.engine.graphics.WindowMaximizedEvent
import nz.net.ultraq.redhorizon.engine.input.KeyEvent
import nz.net.ultraq.redhorizon.events.EventTarget
import nz.net.ultraq.redhorizon.filetypes.AnimationFile
import nz.net.ultraq.redhorizon.filetypes.ImageFile
import nz.net.ultraq.redhorizon.filetypes.ImagesFile
import nz.net.ultraq.redhorizon.filetypes.Palette
import nz.net.ultraq.redhorizon.filetypes.SoundFile
import nz.net.ultraq.redhorizon.filetypes.Streaming
import nz.net.ultraq.redhorizon.filetypes.VideoFile
import nz.net.ultraq.redhorizon.geometry.Dimension
import nz.net.ultraq.redhorizon.media.Animation
import nz.net.ultraq.redhorizon.media.Image
import nz.net.ultraq.redhorizon.media.ImageStrip
import nz.net.ultraq.redhorizon.media.Playable
import nz.net.ultraq.redhorizon.media.SoundEffect
import nz.net.ultraq.redhorizon.media.SoundTrack
import nz.net.ultraq.redhorizon.media.Video

import imgui.ImGui
import imgui.type.ImBoolean
import org.joml.Vector3f
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.*

import groovy.json.JsonSlurper
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

	private final List<MixData> mixDatabase

	private File currentDirectory
	private MixFile currentMixFile
	private final List<Entry> fileEntries = new CopyOnWriteArrayList<>()
	private InputStream selectedFileInputStream
	private Object selectedFileClass
	private Object selectedMedia
	private Palette palette
	private int tick

	/**
	 * Constructor, sets up an application with the default configurations.
	 */
	Explorer() {

		super(
			new AudioConfiguration(),
			new GraphicsConfiguration(
				maximized: userPreferences.get(ExplorerPreferences.WINDOW_MAXIMIZED),
				renderResolution: new Dimension(800, 500)
			)
		)

		def mixJsonData = getResourceAsStream("mix-data.json").withBufferedStream { it.text }
		mixDatabase = new JsonSlurper().parseText(mixJsonData).collect { data -> data as MixData }

		buildList(new File(System.getProperty("user.dir")))

		// TODO: Be able to choose which palette to apply to a paletted file
		palette = getResourceAsStream(PaletteType.RA_TEMPERATE.file).withBufferedStream { inputStream ->
			return new PalFile(inputStream)
		}
	}

	/**
	 * Update the contents of the list from the current directory.
	 * 
	 * @param directory
	 */
	private void buildList(File directory) {

		fileEntries.clear()
		currentMixFile = null

		if (directory.parent) {
			fileEntries << new Entry('/..')
		}
		directory.listFiles()
			.sort { file1, file2 ->
				file1.directory && !file2.directory ? -1 :
					!file1.directory && file2.directory ? 1 :
					file1.name <=> file2.name
			}
			.each { fileOrDirectory ->
				fileEntries << new Entry(fileOrDirectory.directory ? "/${fileOrDirectory.name}" : fileOrDirectory.name)
			}

		currentDirectory = directory
	}

	/**
	 * Update the contents of the list from the current mix file.
	 * 
	 * @param mixFile
	 */
	private void buildList(MixFile mixFile) {

		fileEntries.clear()

		fileEntries << new Entry('/..')
		mixFile.entries.each { entry ->
			def hexId = "0x${Integer.toHexString(entry.id)}"
			def matchingData = mixDatabase.find { it.id == hexId }
			fileEntries << (matchingData ?
				new Entry(matchingData.name) :
				new Entry("(unknown entry, ID: ${hexId}, size: ${entry.size}", entry.id.toString())
			)
		}

		currentMixFile = mixFile
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
	 * Update the preview are for the given file data and type.
	 * 
	 * @param fileType
	 */
	private void preview(Object fileType) {

		switch (fileType) {

			case VideoFile:
				def video = new Video(fileType, gameClock)
					.scaleXY(2)
					.translate(-fileType.width / 2, -fileType.height / 2)
				scene << video
				video.play()
				selectedMedia = video
				break

			case AnimationFile:
				def animation = new Animation(fileType, gameClock)
					.scaleXY(2)
					.translate(-fileType.width / 2, -fileType.height / 2)
				scene << animation
				animation.play()
				selectedMedia = animation
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
				// Try determine the appropriate media for the sound file
				def sound = fileType instanceof AudFile && fileType.uncompressedSize > 1048576 ? // 1MB
					new SoundTrack(fileType, gameClock) :
					new SoundEffect(fileType)
				scene << sound
				sound.play()
				selectedMedia = sound
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

		// Clear the previous entry and reset the preview scene
		clearCurrentFile()
		scene.clear()
		graphicsEngine.camera.center(new Vector3f())

		logger.info('Loading entry from mix file with name: {}, ID: 0x{}...', entry.name ?: '(unknown)', Integer.toHexString(entry.id))

		def entryClass = entry.name?.getFileClass()
		if (!entryClass) {
			logger.info('No filetype implementation for {}', entry.name ?: '(unknown)')
			return
		}

		selectedFileInputStream = currentMixFile.getEntryData(entry)
		preview(entryClass.newInstance(selectedFileInputStream))
	}

	/**
	 * Update the preview area with the media for the selected file.
	 * 
	 * @param file
	 */
	private void previewFile(File file) {

		// Clear the previous file and reset the preview scene
		clearCurrentFile()
		scene.clear()
		graphicsEngine.camera.center(new Vector3f())

		logger.info('Loading {}...', file.name)

		def fileClass = file.name.getFileClass()
		if (!fileClass) {
			logger.info('No filetype implementation for {}', file.name)
			return
		}

		selectedFileInputStream = file.newInputStream()
		preview(fileClass.newInstance(selectedFileInputStream))
	}

	@Override
	void run() {

		def explorerGuiRenderPass = new ExplorerGuiRenderPass()

		// Include the explorer GUI in the render pipeline
		graphicsEngine.on(EngineLoopStartEvent) { event ->
			inputEventStream.on(KeyEvent) { keyEvent ->
				if (keyEvent.action == GLFW_PRESS) {
					if (keyEvent.key == GLFW_KEY_O) {
						explorerGuiRenderPass.enabled = !explorerGuiRenderPass.enabled
					}
				}
			}
			graphicsEngine.renderPipeline.addOverlayPass(explorerGuiRenderPass)
		}
		graphicsEngine.on(WindowMaximizedEvent) { event ->
			userPreferences.set(ExplorerPreferences.WINDOW_MAXIMIZED, event.maximized)
		}

		// Handle events from the explorer GUI
		explorerGuiRenderPass.on(EntrySelectedEvent) { event ->
			if (currentMixFile) {
				def selectedEntry = currentMixFile.getEntry(event.entry.value)
				previewEntry(selectedEntry)
			}
			else {
				def selectedFile = new File(currentDirectory, event.entry.value)
				if (selectedFile.directory) {
					buildList(selectedFile)
				}
				else if (selectedFile.name.endsWith('.mix')) {
					buildList(new MixFile(selectedFile))
				}
				else {
					previewFile(selectedFile)
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

	/**
	 * A render pass for drawing the ImGui explorer elements.
	 */
	private class ExplorerGuiRenderPass implements EventTarget, OverlayRenderPass {

		boolean enabled = true

		private Entry selectedEntry

		@Override
		void render(GraphicsRenderer renderer, Framebuffer sceneResult) {

			ImGui.begin('Current directory', new ImBoolean(true))

			// File list
			ImGui.beginListBox('##FileList', -Float.MIN_VALUE, -Float.MIN_VALUE)
			fileEntries.each { entry ->
				def isSelected = selectedEntry == entry
				if (ImGui.selectable(entry.name, isSelected)) {
					ImGui.setItemDefaultFocus()
					selectedEntry = entry
					trigger(new EntrySelectedEvent(entry))
				}
			}
			ImGui.endListBox()

			ImGui.end()
		}
	}
}
