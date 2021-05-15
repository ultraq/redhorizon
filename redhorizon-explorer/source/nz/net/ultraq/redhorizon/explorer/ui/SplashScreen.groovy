/* 
 * Copyright 2012, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import org.eclipse.swt.SWT
import org.eclipse.swt.graphics.GC
import org.eclipse.swt.graphics.Image
import org.eclipse.swt.graphics.Rectangle
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Label
import org.eclipse.swt.widgets.Shell

/**
 * Main splash screen window.
 * 
 * @author Emanuel Rabina
 */
class SplashScreen {

	private final Display display
	private final Shell shell

	/**
	 * Constructor, create a new splash screen with the default image and text.
	 * 
	 * @param version
	 */
	SplashScreen(String version) {

		display = new Display()
		shell = new Shell(display, SWT.ON_TOP)

		// Splash screen frame
		shell.background = display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND)
		shell.backgroundMode = SWT.INHERIT_FORCE
		shell.layout = new GridLayout(2, false).with {
			marginWidth       = 0
			marginHeight      = 0
			marginTop         = 0
			marginLeft        = 0
			marginRight       = 0
			marginBottom      = 0
			horizontalSpacing = 0
			verticalSpacing   = 0
			return it
		}

		// Splash screen image
		getResourceAsStream('nz/net/ultraq/redhorizon/explorer/ui/SplashScreen.png').withBufferedStream { imageInputStream ->
			def splashScreenImage = new Image(display, 600, 350)
			def gc = new GC(splashScreenImage)
			gc.drawImage(new Image(display, imageInputStream), 0, 0, 600, 350, 0, 0, 600, 350)
			gc.dispose()
			new Label(shell, SWT.NONE).with {
				image = splashScreenImage
				layoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1)
			}
		}

		// Text area
		def textGroup = new Composite(shell, SWT.NONE).with {
			layout = new GridLayout(2, false).with {
				marginWidth       = 0
				marginHeight      = 0
				marginTop         = 5
				marginLeft        = 5
				marginRight       = 5
				marginBottom      = 5
				horizontalSpacing = 0
				verticalSpacing   = 0
				return it
			}
			layoutData = new GridData(SWT.FILL, SWT.FILL, true, true)
			return it
		}

		// Task text area
		new Label(textGroup, SWT.SHADOW_OUT).with {
			text = 'Red Horizon Explorer 🔎'
			layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false)
		}

		// Version text area
		new Label(textGroup, SWT.NONE).with {
			text = version
			layoutData = new GridData(SWT.RIGHT, SWT.CENTER, false, false)
		}
	}

	/**
	 * Close the splash screen.
	 */
	void close() {

		display.syncExec { ->
			shell.dispose()
		}
	}

	/**
	 * Opens the splash screen.  This method will then block until some action
	 * closes this screen via the {@link #close()} method.
	 */
	void open() {

		// Center on screen and open
		shell.pack()
		def resolution = display.primaryMonitor.bounds
		def window = shell.bounds
		shell.bounds = new Rectangle(
			(resolution.width >> 1)  - (window.width >> 1),
			(resolution.height >> 1) - (window.height >> 1),
			window.width, window.height)
		shell.open()

		// Wait until closed
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep()
			}
		}
		display.dispose()
	}
}
