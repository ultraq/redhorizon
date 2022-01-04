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
import nz.net.ultraq.redhorizon.geometry.Dimension
import nz.net.ultraq.redhorizon.media.Animation
import nz.net.ultraq.redhorizon.media.Image
import nz.net.ultraq.redhorizon.media.ImageStrip
import nz.net.ultraq.redhorizon.media.Playable
import nz.net.ultraq.redhorizon.media.SoundEffect
import nz.net.ultraq.redhorizon.media.SoundTrack

import imgui.ImGui
import imgui.type.ImBoolean
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

	private File currentDirectory
	private final List<String> fileNames = new CopyOnWriteArrayList<>()
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

		currentDirectory = new File(System.getProperty("user.dir"))
		buildList()

		// TODO: Be able to choose which palette to apply to a paletted file
		palette = getResourceAsStream(PaletteType.RA_TEMPERATE.file).withBufferedStream { inputStream ->
			return new PalFile(inputStream)
		}
	}

	/**
	 * Update the contents of the list from the current directory.
	 */
	private void buildList() {

		fileNames.clear()

		if (currentDirectory.parent) {
			fileNames << '/..'
		}
		currentDirectory.listFiles()
			.sort { file1, file2 ->
				file1.directory && !file2.directory ? -1 :
					!file1.directory && file2.directory ? 1 :
					file1.name <=> file2.name
			}
			.each { fileOrDirectory ->
				fileNames << (fileOrDirectory.isDirectory() ? "/${fileOrDirectory.name}" : fileOrDirectory.name)
			}
	}

	/**
	 * Update the preview area with something appropriate for the selected file.
	 * 
	 * @param newFile
	 */
	private void previewFile(File newFile) {

		// Clear the previous file
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

			scene.clear()
			graphicsEngine.camera.center(new Vector3f())
		}

		logger.info('Loading {}...', newFile.name)

		if (newFile.file) {
			def fileClass = newFile.name.getFileClass()
			if (!fileClass) {
				logger.info('No filetype implementation for {}', newFile.name)
				return
			}

			if (fileClass == MixFile) {
				selectedFileClass = fileClass.newInstance(newFile)
			}
			else {
				selectedFileInputStream = newFile.newInputStream()
				selectedFileClass = fileClass.newInstance(selectedFileInputStream)

				switch (selectedFileClass) {

					case AnimationFile:
						def animation = new Animation(selectedFileClass, gameClock)
							.translate(-selectedFileClass.width / 2, -selectedFileClass.height / 2)
						scene << animation
						animation.play()
						selectedMedia = animation
						break

					case ImageFile:
						def image = new Image(selectedFileClass)
						scene << image
							.translate(-selectedFileClass.width / 2, -selectedFileClass.height / 2)
						selectedMedia = image
						break

					case ImagesFile:
						tick = selectedFileClass.width
						def imageStrip = new ImageStrip(selectedFileClass, palette)
						scene << imageStrip
						selectedMedia = imageStrip
						break

					case SoundFile:
						// Try determine the appropriate media for the sound file
						def sound = selectedFileClass instanceof AudFile && selectedFileClass.uncompressedSize > 1048576 ? // 1MB
							new SoundTrack(selectedFileClass, gameClock) :
							new SoundEffect(selectedFileClass)
						scene << sound
						sound.play()
						selectedMedia = sound
						break

					default:
						logger.info('Filetype of {} not yet configured', selectedFileClass.class.simpleName)
						selectedMedia = null
				}
			}

			// TODO: Display selected file info elsewhere?  Currently these are long
			//       lines of text 🤔
//			logger.info('{}', selectedFileClass)
		}
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
		explorerGuiRenderPass.on(FileSelectedEvent) { event ->
			def newFile = new File(currentDirectory, event.selectedFile)
			if (newFile.directory) {
				currentDirectory = newFile
				buildList()
			}
			else {
				previewFile(newFile)
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
	}

	/**
	 * A render pass for drawing the ImGui explorer elements.
	 */
	private class ExplorerGuiRenderPass implements EventTarget, OverlayRenderPass {

		boolean enabled = true

		private String selectedFileName

		@Override
		void render(GraphicsRenderer renderer, Framebuffer sceneResult) {

			ImGui.begin('Current directory', new ImBoolean(true))

			// File list
			ImGui.beginListBox('##FileList', -Float.MIN_VALUE, -Float.MIN_VALUE)
			fileNames.each { fileName ->
				def isSelected = this.selectedFileName == fileName
				if (ImGui.selectable(fileName, isSelected)) {
					ImGui.setItemDefaultFocus()
					this.selectedFileName = fileName
					trigger(new FileSelectedEvent(fileName))
				}
			}
			ImGui.endListBox()

			ImGui.end()
		}
	}
}
