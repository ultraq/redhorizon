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
import nz.net.ultraq.redhorizon.engine.graphics.Framebuffer
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.OverlayRenderPass
import nz.net.ultraq.redhorizon.engine.graphics.WindowMaximizedEvent
import nz.net.ultraq.redhorizon.engine.input.KeyEvent
import nz.net.ultraq.redhorizon.events.EventTarget
import nz.net.ultraq.redhorizon.filetypes.ImageFile
import nz.net.ultraq.redhorizon.geometry.Dimension
import nz.net.ultraq.redhorizon.media.Image

import imgui.ImGui
import imgui.type.ImBoolean
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE
import static org.lwjgl.glfw.GLFW.GLFW_KEY_O
import static org.lwjgl.glfw.GLFW.GLFW_PRESS

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
	private final List<String> fileNames = []
	private Object selectedFile

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
			updatePreviewFile(event.selectedFile)
		}

		// Key event handler
		inputEventStream.on(KeyEvent) { event ->
			if (event.action == GLFW_PRESS) {
				switch (event.key) {
					case GLFW_KEY_ESCAPE:
						stop()
						break
				}
			}
		}
	}

	/**
	 * Update the preview area using the selected item.
	 * 
	 * @param selectedFileName
	 */
	private void updatePreviewFile(String selectedFileName) {

		// Clear the previous file
		if (selectedFile) {
			selectedFile = null
			scene.clear()
		}

		logger.info('Loading {}...', selectedFileName)

		def selectedItem = new File(currentDirectory, selectedFileName)
		if (selectedItem.file) {
			def fileClass = selectedItem.name.getFileClass()
			if (!fileClass) {
				logger.info('No filetype implementation for {}', selectedItem.name)
				return
			}

			if (fileClass == MixFile) {
				selectedFile = fileClass.newInstance(selectedItem)
			}
			else {
				selectedItem.withInputStream { inputStream ->
					selectedFile = fileClass.newInstance(inputStream)
					switch (selectedFile) {
						case ImageFile:
							scene << new Image(selectedFile)
								.translate(-selectedFile.width / 2, -selectedFile.height / 2)
							break
						default:
							logger.info('Filetype of {} not yet configured', selectedFile.class.simpleName)
					}
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
