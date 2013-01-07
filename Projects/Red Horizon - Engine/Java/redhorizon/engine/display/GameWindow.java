/*
 * Copyright 2013, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package redhorizon.engine.display;

import redhorizon.ui.Window;
import redhorizon.utilities.Animator;
import redhorizon.utilities.AnimatorTask;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.opengl.GLData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import java.util.HashMap;

/**
 * A window containing the canvas onto which the graphics engine will be
 * rendering to.  This window drives the rendering loop, as well as the input
 * events that will be dispatched to the input subsystem.
 * <p>
 * The main focus of this window will be the drawing area, and until a proper
 * in-game menu system can be developed, the menu will be provided by the usual
 * shell menu (File, Edit, etc).
 * 
 * @author Emanuel Rabina
 */
public class GameWindow extends Window {

	private static final String WINDOW_TITLE = "Red Horizon";
	private static final int CANVAS_WIDTH  = 1024;
	private static final int CANVAS_HEIGHT = 768;

	// SWT components
	private final GLCanvas canvas;

	// Mouse-related
	private Animator cursoranimator;
	private boolean offscreen;

	// Window event delegates
	private RenderingDelegate renderingdelegate;
	private InputEventDelegate inputeventdelegate;

	/**
	 * Constructor, creates the window that produces all display and input
	 * events.
	 */
	public GameWindow() {

		super(SWT.CLOSE | SWT.TITLE | SWT.MIN | SWT.DOUBLE_BUFFERED);

		shell.setText(WINDOW_TITLE);
		shell.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
		shell.setBackgroundMode(SWT.INHERIT_DEFAULT);
		shell.addShellListener(new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent event) {
				close();
				event.doit = false;
			}
		});

		// Set up the OpenGL canvas
		GLData gldata = new GLData();
		gldata.doubleBuffer = true;
		gldata.sampleBuffers = 1;
		gldata.samples = 4;
		canvas = new GLCanvas(shell, SWT.NONE, gldata);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() {

		if (!shell.isDisposed()) {
			super.close();
			renderingdelegate.displayClosed();
		}
	}

	/**
	 * Return the height of the canvas the graphics will be drawn on.
	 * 
	 * @return OpenGL canvas height.
	 */
	public int getCanvasHeight() {

		return CANVAS_HEIGHT;
	}

	/**
	 * Return the width of the canvas the graphics will be drawn on.
	 * 
	 * @return OpenGL canvas width.
	 */
	public int getCanvasWidth() {

		return CANVAS_WIDTH;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void open() {

		pack();
		shell.open();

		canvas.setCurrent();
		renderingdelegate.displayInit();

		// Perform rendering loop
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				if (!shell.isDisposed()) {
					renderingdelegate.displayRendering();
					canvas.swapBuffers();
					display.timerExec(5, this);
				}
			}
		});

		// Don't hog CPU cycles
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void pack() {

		// Size the window and canvas
		shell.setLayout(createGridLayout(1, true, 0, 0, 0, 0, 0, 0));
		canvas.setLayoutData(new GridData(CANVAS_WIDTH, CANVAS_HEIGHT));

		super.pack();
	}

	/**
	 * Update the mouse cursor to use the one specified.
	 * 
	 * @param cursor
	 */
	public void setCursor(CursorTypes cursor) {

		// Do nothing if no change in cursor
		if (cursor == currentcursor) {
			return;
		}

		// Stop current animator
		if (cursoranimator != null) {
			cursoranimator.stop();
		}

		Cursor[] cursorimages = cursors.get(cursor);

		// Start new cursor animator for animated cursors
		if (cursorimages.length > 1) {
			SWTCursorAnimator handler = new SWTCursorAnimator(cursorimages);
			cursoranimator = new Animator("GameWindowSWT - Cursor animator",
					cursorimages.length * 200, Animator.CYCLE_INFINITE);
			cursoranimator.addTask(handler);
			cursoranimator.start();
		}

		// Set the cursor image for static cursors
		else {
			cursoranimator = null;
			display.syncExec(new Runnable() {
				@Override
				public void run() {
					shell.setCursor(cursorimages[0]);
				}
			});
		}
	}

	/**
	 * Sets the delegate for handling input events from the window.
	 * 
	 * @param inputeventdelegate
	 */
	public void setInputEventHandler(InputEventDelegate inputeventdelegate) {

		// Clear prior listeners
		if (this.inputeventdelegate != null) {
			shell.removeKeyListener(this.inputeventdelegate);
			shell.removeMouseListener(this.inputeventdelegate);
			canvas.removeMouseMoveListener(this.inputeventdelegate);
			shell.removeMouseTrackListener(this.inputeventdelegate);
		}

		// Set the new listeners
		shell.addKeyListener(inputeventdelegate);
		shell.addMouseListener(inputeventdelegate);
		canvas.addMouseMoveListener(inputeventdelegate);
		shell.addMouseTrackListener(inputeventdelegate);
		this.inputeventdelegate = inputeventdelegate;
	}

	/**
	 * Sets the delegate for handling rendering events from the window.
	 * 
	 * @param renderingdelegate
	 */
	public void setRenderingCallback(RenderingDelegate renderingdelegate) {

		this.renderingdelegate = renderingdelegate;
	}

	/**
	 * Inner-class for handling the animation of SWT cursors over the game
	 * window.
	 */
	private class SWTCursorAnimator implements AnimatorTask {

		private final Cursor[] cursorimages;
		private int currentimage = 0;

		/**
		 * Constructor, loads a cursor to animate
		 * 
		 * @param cursorimages Array of cursors representing the frames of this
		 * 					   cursor's animation.
		 */
		private SWTCursorAnimator(Cursor[] cursorimages) {

			this.cursorimages = cursorimages;
		}

		/**
		 * Sets the initial cursor.
		 */
		public void begin() {

			setCursor0(cursorimages[0]);
		}

		/**
		 * Does nothing.
		 */
		public void end() {
		}

		/**
		 * Updates an animated cursor's image as necessary.
		 * 
		 * @param fraction Value close to which frame to display.
		 */
		public void event(float fraction) {

			final int closestimage = (int)(fraction * cursorimages.length);

			// Do nothing if no change in cursor used
			if (currentimage == closestimage) {
				return;
			}

			// Update cursor
			setCursor0(cursorimages[closestimage]);
			currentimage = closestimage;
		}
	}
}
