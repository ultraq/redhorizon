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

import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Menu
import org.eclipse.swt.widgets.MenuItem
import org.eclipse.swt.widgets.Shell
import static org.eclipse.swt.SWT.*

/**
 * The main application window which contains the file tree on the left, and a
 * preview pane on the right.
 * 
 * @author Emanuel Rabina
 */
class ExplorerWindow {

	private final Shell shell

	/**
	 * Build the application window UI.
	 * 
	 * @param display
	 */
	ExplorerWindow(Display display) {

		shell = new Shell(display, SHELL_TRIM)
		shell.text = 'Red Horizon Explorer 🔎'

		shell.menuBar = new Menu(shell, BAR).with { menuBar ->
			def fileMenuItem = new MenuItem(menuBar, CASCADE)
			fileMenuItem.text = '&File'

			def fileMenu = new Menu(shell, DROP_DOWN)
			fileMenuItem.menu = fileMenu
			new MenuItem(fileMenu, PUSH).with { exitMenuItem ->
				text = "E&xit"
				addListener(Selection) { event ->
					close(display)
				}
			}

			return menuBar
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
	 * Open the application window and wait until it is closed.
	 * 
	 * @param display
	 */
	void open(Display display) {

		shell.openCentered(display)

		// Wait until closed
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep()
			}
		}
	}
}
