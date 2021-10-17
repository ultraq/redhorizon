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

package nz.net.ultraq.redhorizon.explorer.ui

import nz.net.ultraq.redhorizon.classic.filetypes.mix.MixFile
import nz.net.ultraq.redhorizon.filetypes.FileExtensions

import org.eclipse.swt.graphics.Point
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Button
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Group
import org.eclipse.swt.widgets.Label
import org.eclipse.swt.widgets.List
import org.eclipse.swt.widgets.Menu
import org.eclipse.swt.widgets.MenuItem
import org.eclipse.swt.widgets.Shell
import org.reflections.Reflections
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.eclipse.swt.SWT.*

/**
 * The main application window which contains the file tree on the left, and a
 * preview pane on the right.
 * 
 * @author Emanuel Rabina
 */
class ExplorerWindow {

	private static final Logger logger = LoggerFactory.getLogger(ExplorerWindow)
	private static final String TITLE = 'Red Horizon Explorer 🔎'

	private final Shell shell
	private final List fileList
	private final Label selectedItemLabel
	private final Button selectedItemButton
	private File currentDirectory
	private Object selectedFile

	/**
	 * Build the application window UI.
	 * 
	 * @param display
	 */
	ExplorerWindow(Display display) {

		currentDirectory = new File(System.getProperty("user.dir"))

		shell = new Shell(display, SHELL_TRIM)
		updateTitle(currentDirectory.toString())

		shell.menuBar = new Menu(shell, BAR).with { menuBar ->
			new MenuItem(menuBar, CASCADE).with { fileMenuItem ->
				text = '&File'
				menu = new Menu(shell, DROP_DOWN).with { fileMenu ->
					new MenuItem(fileMenu, PUSH).with { exitMenuItem ->
						text = "E&xit"
						addListener(Selection) { event ->
							close(display)
						}
					}
					return fileMenu
				}
				return fileMenuItem
			}
			return menuBar
		}

		shell.layout = new GridLayout(2, false).with {
			marginHeight = 10
			marginWidth = 10
			return it
		}
		shell.size = new Point(600, 600)

		// File/folder explorer
		def pathGroup = new Group(shell, SHADOW_ETCHED_IN).with {
			text = 'Files'
			layout = new GridLayout()
			layoutData = new GridData(LEFT, FILL, false, true).with {
				widthHint = 250
				return it
			}
			return it
		}

		fileList = new List(pathGroup, BORDER | SINGLE | V_SCROLL).with {
			layoutData = new GridData(FILL, FILL, true, true)

			// Selection handler for updating the preview pane
			addListener(Selection) { event ->

				// Close the previous file
				if (selectedFile instanceof Closeable) {
					selectedFile.close()
				}

				def selectedItem = new File(currentDirectory, getItem(selectionIndex))
				if (selectedItem.isFile()) {
					def fileClass = getFileClass(selectedItem.name)
					if (fileClass) {
						if (fileClass == MixFile) {
							selectedFile = fileClass.newInstance(selectedItem)
							selectedItemButton.enabled = false
						}
						else {
							selectedItem.withInputStream { inputStream ->
								selectedFile = fileClass.newInstance(inputStream)
								selectedItemButton.enabled = true
							}
						}
						selectedItemLabel.text = selectedFile.toString()
					}
					else {
						selectedItemLabel.text = '(unknown file type)'
						selectedItemButton.enabled = false
					}
				}
			}

			// Double-click handler for changing directories
			addListener(MouseDoubleClick) { event ->
				if (selectionIndex == 0) {
					currentDirectory = currentDirectory.parentFile
					updateTitle(currentDirectory.toString())
					buildList()
				}
				else {
					def selectedItem = new File(currentDirectory, getItem(selectionIndex))
					if (selectedItem.isDirectory()) {
						currentDirectory = selectedItem
						updateTitle(currentDirectory.toString())
						buildList()
					}
				}
			}
			return it
		}
		buildList()

		// Selected file preview
		def previewGroup = new Group(shell, DEFAULT).with {
			text = 'Details'
			layout = new GridLayout()
			layoutData = new GridData(FILL, FILL, true, true)
			size = new Point(400, 400)
			return it
		}

		selectedItemLabel = new Label(previewGroup, CENTER | WRAP).with {
			layoutData = new GridData(FILL, BOTTOM, true, true).with {
				minimumHeight = 35
				return it
			}
			text = '(item preview)'
			return it
		}

		selectedItemButton = new Button(previewGroup, CENTER | PUSH).with {
			enabled = false
			layoutData = new GridData(CENTER, TOP, true, true)
			text = 'Open'
			return it
		}
	}

	/**
	 * Update the contents of the list from the current directory.
	 */
	private void buildList() {

		fileList.removeAll()
		fileList.add('..')
		currentDirectory.listFiles()
			.sort { file1, file2 ->
				return file1.directory && !file2.directory ? -1 :
					!file1.directory && file2.directory ? 1 :
					file1.name <=> file2.name
			}
			.each { fileOrDirectory ->
				fileList.add(fileOrDirectory.isDirectory() ?
					"/${fileOrDirectory.name}" :
					fileOrDirectory.name)
			}
	}

	/**
	 * Close the application window.
	 * 
	 * @param display
	 */
	void close(Display display) {

		display.syncExec { ->
			shell.dispose()
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

	/**
	 * Open the application window and wait until it is closed.
	 * 
	 * @param display
	 */
	void open(Display display) {

		shell.openCentered(display, false)

		// Wait until closed
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep()
			}
		}
	}

	/**
	 * Update the window title to include the current path.
	 * 
	 * @param currentPath
	 */
	private final void updateTitle(String currentPath) {

		shell.text = "${TITLE} - ${currentPath}"
	}
}
