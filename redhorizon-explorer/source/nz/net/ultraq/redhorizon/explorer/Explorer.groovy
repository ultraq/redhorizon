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
import nz.net.ultraq.redhorizon.engine.input.InputEventStream
import nz.net.ultraq.redhorizon.engine.input.KeyEvent
import nz.net.ultraq.redhorizon.filetypes.FileExtensions
import nz.net.ultraq.redhorizon.geometry.Dimension

import imgui.ImGui
import imgui.type.ImBoolean
import org.reflections.Reflections
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
	private final List<String> fileList = []
	private int selectedFileIndex
	private int lastSelectedFileIndex
	private Object selectedFile

	/**
	 * Open the explorer.
	 * 
	 * @param args
	 */
	static void main(String[] args) {

//		def splashScreen = new SplashScreen(commandSpec.version()[0] ?: '(development)')
//		Executors.newSingleThreadExecutor().executeAndShutdown { executorService ->
//			executorService.execute { ->
//				Thread.sleep(commandSpec.version()[0] != null ? 3000 : 1000)
//				splashScreen.close()
//			}
//			splashScreen.open()
//		}
		new Explorer().start()
		System.exit(0)
	}

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

		fileList.clear()

		if (currentDirectory.parent) {
			fileList << '/..'
		}
		currentDirectory.listFiles()
			.sort { file1, file2 ->
				return file1.directory && !file2.directory ? -1 :
					!file1.directory && file2.directory ? 1 :
						file1.name <=> file2.name
			}
			.each { fileOrDirectory ->
				fileList << (fileOrDirectory.isDirectory() ? "/${fileOrDirectory.name}" : fileOrDirectory.name)
			}
	}

	/**
	 * Find the appropriate class for reading a file with the given name.
	 *
	 * @param filename
	 * @return
	 */
	private static Class<?> getFileClass(String filename) {

		def suffix = filename.substring(filename.lastIndexOf('.') + 1)
		def fileClass = new Reflections(
			'nz.net.ultraq.redhorizon.filetypes',
			'nz.net.ultraq.redhorizon.classic.filetypes'
		)
			.getTypesAnnotatedWith(FileExtensions)
			.find { type ->
				def annotation = type.getAnnotation(FileExtensions)
				return annotation.value().any { extension ->
					return extension.equalsIgnoreCase(suffix)
				}
			}
		if (!fileClass) {
			logger.debug('No implementation for {} filetype', suffix)
		}
		return fileClass
	}

	@Override
	void run() {

		graphicsEngine.on(EngineLoopStartEvent) { event ->
			graphicsEngine.renderPipeline.addOverlayPass(new ExplorerGuiRenderPass(inputEventStream))
		}
		graphicsEngine.on(WindowMaximizedEvent) { event ->
			userPreferences.set(ExplorerPreferences.WINDOW_MAXIMIZED, event.maximized)
		}
	}

	private void updatePreviewFile(int selectionIndex) {

		// Close the previous file
		if (selectedFile instanceof Closeable) {
			selectedFile.close()
		}

		def selectedItem = new File(currentDirectory, fileList[selectionIndex])
		if (selectedItem.isFile()) {
			def fileClass = getFileClass(selectedItem.name)
			if (fileClass) {
				if (fileClass == MixFile) {
					selectedFile = fileClass.newInstance(selectedItem)
//					selectedItemButton.enabled = false
				}
				else {
					selectedItem.withInputStream { inputStream ->
						selectedFile = fileClass.newInstance(inputStream)
//						selectedItemButton.enabled = true
					}
				}
//				selectedItemLabel.text = selectedFile.toString()
			}
			else {
//				selectedItemLabel.text = '(unknown file type)'
//				selectedItemButton.enabled = false
			}
		}
	}

	/**
	 * A render pass for drawing the ImGui explorer elements.
	 */
	private class ExplorerGuiRenderPass implements OverlayRenderPass {

		boolean enabled = true

		ExplorerGuiRenderPass(InputEventStream inputEventStream) {

			inputEventStream.on(KeyEvent) { event ->
				if (event.action == GLFW_PRESS) {
					if (event.key == GLFW_KEY_O) {
						this.enabled = !this.enabled
					}
				}
			}
		}

		@Override
		void render(GraphicsRenderer renderer, Framebuffer sceneResult) {

			ImGui.begin('Current directory', new ImBoolean(true))

			// File list
			ImGui.beginListBox('##FileList', -Float.MIN_VALUE, -Float.MIN_VALUE)
			fileList.eachWithIndex { fileItem, index ->
				def isSelected = selectedFileIndex == index
				if (ImGui.selectable(fileItem, isSelected)) {
					selectedFileIndex = index
					if (lastSelectedFileIndex != selectedFileIndex) {
						Explorer.this.updatePreviewFile(selectedFileIndex)
					}
				}
				if (isSelected) {
					ImGui.setItemDefaultFocus()
				}
			}
			ImGui.endListBox()

			ImGui.end()
		}
	}
}
