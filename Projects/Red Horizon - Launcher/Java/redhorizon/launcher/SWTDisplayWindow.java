/*
 * Copyright 2007 Emanuel Rabina (http://www.ultraq.net.nz/)
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

package redhorizon.launcher;

import redhorizon.ui.SWTUtility;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.opengl.GLData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/**
 * SWT implementation of Red Horizon's display window.
 * 
 * @author Emanuel Rabina
 */
public class SWTDisplayWindow extends DisplayWindow {

	private final Display display;
	private final Shell shell;
	private final GLCanvas canvas;

	/**
	 * Constructor, sets the attached display renderer and defaults to a
	 * width/height of 800/600.
	 * 
	 * @param title	   Title of this window.
	 * @param renderer Listener for the rendering event.
	 */
	public SWTDisplayWindow(String title, DisplayRenderer renderer) {

		this(title, 800, 600, renderer);
	}

	/**
	 * Constructor, sets the attached display renderer.
	 * 
	 * @param title	   Title of this window.
	 * @param width	   Horizontal resolution of the OpenGL canvas.
	 * @param height   Vertical resolution of the OpenGL canvas.
	 * @param renderer Listener for the rendering event.
	 */
	public SWTDisplayWindow(String title, int width, int height, final DisplayRenderer renderer) {

		super(renderer);

		display = Display.getDefault();

		// Window
		shell = new Shell(display, SWT.CLOSE | SWT.TITLE | SWT.MIN | SWT.DOUBLE_BUFFERED);
		shell.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
		shell.setBackgroundMode(SWT.INHERIT_DEFAULT);
		shell.setText(title);
		shell.addListener(SWT.Close, new Listener() {
			public void handleEvent(Event event) {
				renderer.displayShutdown();
			}
		});
		shell.setLayout(SWTUtility.createGridLayout());

		// OpenGL pixel descriptor
		GLData gldata = new GLData();
		gldata.doubleBuffer = true;
		gldata.sampleBuffers = 2;
		gldata.samples = 4;

		// OpenGL canvas
		canvas = new GLCanvas(shell, SWT.NONE, gldata);
		canvas.setLayoutData(new GridData(width, height));
	}

	/**
	 * Closes the window, being sure not to interfere with the rendering thread.
	 */
	@Override
	protected void closeImpl() {

		display.syncExec(new Runnable() {
			@Override
			public void run() {
				shell.dispose();
			}
		});
	}

	/**
	 * Centers and displays the window, starting the rendering thread on the
	 * display.
	 */
	@Override
	protected void openImpl() {

		shell.pack();
		SWTUtility.centerShell(shell);
		shell.open();

		// Begin render loop
		canvas.setCurrent();
		renderer.displayStartup();

		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				if (!shell.isDisposed()) {
					renderer.displayRendering();
					canvas.swapBuffers();
					display.timerExec(5, this);
				}
			}
		});

		// Wait until closed
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
}
