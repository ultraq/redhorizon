
// =================================
// Scanner's Java - Main game window
// =================================

package redhorizon.engine.display;

import redhorizon.geometry.Area2D;
import redhorizon.utilities.Animator;
import redhorizon.utilities.AnimatorTask;

import nz.net.ultraq.common.preferences.Preferences;
import nz.net.ultraq.gui.swt.CenterShell;
import nz.net.ultraq.gui.swt.LayoutUtility;

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
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.opengl.GLData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import java.util.HashMap;

/**
 * This is the main rendering window to be used when the game is in windowed
 * mode (which for the majority of the development stage, it will be).
 * <p>
 * The main focus of this window will be the drawing area, and until a proper
 * in-game menu system can be developed, the menu will be provided by the usual
 * OS menu (File, Edit, etc).
 * 
 * @author Emanuel Rabina
 */
@CenterShell
public class GameWindowSWT extends GameWindow {

	// SWT components
	private final Display display;
	private final Shell shell;
	private final GLCanvas canvas;

	// Mouse-related
	private final HashMap<CursorTypes,Cursor[]> cursors = new HashMap<CursorTypes,Cursor[]>();
	private Animator cursoranimator;
	private CursorTypes currentcursor;
	private boolean offscreen;

	// Rendering-related
	private DisplayCallback callback;
	private final int width;
	private final int height;

	/**
	 * Constructor, creates the window, menu items, OpenGL canvas, etc.
	 */
	GameWindowSWT() {

		display = new Display();

		// Window
		shell = new Shell(display, SWT.CLOSE | SWT.TITLE | SWT.MIN | SWT.DOUBLE_BUFFERED);
		shell.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
		shell.setBackgroundMode(SWT.INHERIT_DEFAULT);
		shell.addListener(SWT.Close, new Listener() {
			public void handleEvent(Event event) {
				close();
				event.doit = false;
			}
		});

		// Window layout and dimensions
		try {
			width  = Integer.parseInt(Preferences.get(DisplayPreferences.DISPLAY_WIDTH));
			height = Integer.parseInt(Preferences.get(DisplayPreferences.DISPLAY_HEIGHT));
		}
		catch (NumberFormatException nfe) {
			display.dispose();
			throw nfe;
		}
		shell.setLayout(LayoutUtility.createLayout(1, true, new Rectangle(0,0,0,0), new Rectangle(0,0,7,7)));
		shell.setLayoutData(LayoutUtility.createLayoutData(new Rectangle(0, 0, width, height)));

		// Load all of the cursor images
		ImageLoader imageloader = new ImageLoader();
		for (CursorTypes cursortype: CursorTypes.values()) {
			Cursor[] cursorimages;

			String cursorpath = config.getProperty("gamewindow.swt.cursor." + cursortype.propertyname);

			// Map unconfigured cursors to the system cursor
			if (cursorpath == null || cursorpath.trim().length() == 0) {
				cursorimages = new Cursor[]{ display.getSystemCursor(SWT.CURSOR_ARROW) };
			}

			// Create and store new cursors
			else {
				ImageData[] images;
				try {
					images = imageloader.load(cursorpath);
				}
				catch (SWTException swte) {
					display.dispose();
					throw swte;
				}
				cursorimages = new Cursor[images.length];
				for (int i = 0; i < images.length; i++) {
					ImageData image = images[i];
					cursorimages[i] = new Cursor(display, image,
							(int)(image.width * cursortype.hotspotx),
							(int)(image.height * cursortype.hotspoty));
				}
			}

			cursors.put(cursortype, cursorimages);
		}

		// Set the default cursor
		currentcursor = CursorTypes.DEFAULT;

		// OpenGL pixel descriptor
		GLData gldata = new GLData();
		gldata.doubleBuffer = true;
		gldata.sampleBuffers = 1;
		gldata.samples = 4;

		// OpenGL canvas
		canvas = new GLCanvas(shell, SWT.NONE, gldata);
		canvas.setLayoutData(LayoutUtility.createLayoutData(new Rectangle(0, 0, width, height),
				LayoutUtility.HAlign.LEFT, LayoutUtility.VAlign.TOP, false, false));
	}

	/**
	 * @inheritDoc
	 */
	public void close() {

		if (!shell.isDisposed()) {
			display.syncExec(new Runnable() {
				public void run() {
					shell.dispose();
				}
			});
			callback.displayClosed();
		}
	}

	/**
	 * @inheritDoc
	 */
	public Area2D getRenderingArea() {

		return new Area2D(width, height);
	}

	/**
	 * Returns <tt>false</tt> as this implementation does not do full-screen.
	 * 
	 * @return <tt>false</tt>.
	 */
	public boolean isFullScreenSupported() {

		return false;
	}

	/**
	 * @inheritDoc
	 */
	public void open() {

		shell.pack();
		shell.open();

		canvas.setCurrent();
		callback.displayInit();

		// Create rendering loop
		display.asyncExec(new Runnable() {
			public void run() {
				if (!shell.isDisposed()) {

					// Render scene
					callback.displayRendering();

					// Draw results to canvas and repeat
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

		// Dispose of programmer-created resources
		for (Cursor[] cursorimages: cursors.values()) {
			for (Cursor cursorimage: cursorimages) {
				cursorimage.dispose();
			}
		}

		display.dispose();
	}

	/**
	 * @inheritDoc
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
			setCursor0(cursorimages[0]);
		}
	}

	/**
	 * Sets the given cursor on the game window.
	 * 
	 * @param cursor The cursor to use.
	 */
	private void setCursor0(final Cursor cursor) {

		// Update cursor
		display.syncExec(new Runnable() {
			public void run() {
				shell.setCursor(cursor);
			}
		});
	}

	/**
	 * @inheritDoc
	 */
	public void setInputEventHandler(InputEventHandler handler) {

		final SWTKeyListener keylistener = new SWTKeyListener(handler);
		final SWTMouseListener mouselistener = new SWTMouseListener(handler);

		// Attach input event listeners to the application window
		display.syncExec(new Runnable() {
			public void run() {
				shell.addKeyListener(keylistener);
				shell.addMouseListener(mouselistener);
				canvas.addMouseMoveListener(mouselistener);
				shell.addMouseTrackListener(mouselistener);
			}
		});
	}

	/**
	 * @inheritDoc
	 */
	protected void setRenderingCallback(DisplayCallback callback) {

		this.callback = callback;
	}

	/**
	 * @inheritDoc
	 */
	protected void setWindowTitle(String title) {

		shell.setText(title);
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

	/**
	 * Inner-class for handling keyboard events on the game window.
	 */
	private class SWTKeyListener implements KeyListener {

		private final InputEventHandler handler;

		/**
		 * Constructor, assigns the keyboard handler.
		 * 
		 * @param handler Keyboard event handler.
		 */
		private SWTKeyListener(InputEventHandler handler) {

			this.handler = handler;
		}

		/**
		 * Method for taking user keyboard presses and translating them into events
		 * for the registered {@link InputEventHandler} to deal with.
		 * 
		 * @param keyevent Details on the keyboard press event that occurred.
		 */
		public void keyPressed(KeyEvent keyevent) {

			handler.keyPressed(keyevent.character, keyevent.keyCode, keyevent.stateMask);
		}

		/**
		 * Method for interpreting keyboard key release events.  Used only for
		 * keyboard modifier keys.
		 * 
		 * @param keyevent Details on the keyboard key release event.
		 */
		public void keyReleased(KeyEvent keyevent) {

			handler.keyReleased(keyevent.stateMask);
		}
	}

	/**
	 * Inner-class for handling mouse events on the game window.
	 */
	private class SWTMouseListener implements MouseListener, MouseMoveListener, MouseTrackListener {

		private final InputEventHandler handler;

		/**
		 * Constructor, assigns the mouse handler.
		 * 
		 * @param handler Mouse event handler.
		 */
		private SWTMouseListener(InputEventHandler handler) {

			this.handler = handler;
		}

		/**
		 * Method for notifying the input handler of mouse double-click events.
		 * 
		 * @param mouseevent Details of the mouse event that occurred.
		 */
		public void mouseDoubleClick(MouseEvent mouseevent) {

			handler.mouseDoubleClick(mouseevent.button,
					mouseevent.x - (width >> 1), (height >> 1) - mouseevent.y);
		}

		/**
		 * Method for notifying the input handler of mouse down events.
		 * 
		 * @param mouseevent Details on the mouse event that occurred.
		 */
		public void mouseDown(MouseEvent mouseevent) {

			handler.mouseDown(mouseevent.button,
					mouseevent.x - (width >> 1), (height >> 1) - mouseevent.y);
		}

		/**
		 * Checks for when the mouse enters the display area.
		 * 
		 * @param mouseevent Details on the mouse event that occurred.
		 */
		public void mouseEnter(MouseEvent mouseevent) {

			offscreen = false;
		}

		/**
		 * Checks for when the mouse exits the display area.
		 * 
		 * @param mouseevent Details on the mouse event that occurred.
		 */
		public void mouseExit(MouseEvent mouseevent) {

			offscreen = true;

			// Replace off-screen mouse movements with scrolling events
			display.asyncExec(new Runnable() {
				public void run() {

					// Quit
					if (!offscreen || shell.isDisposed()) {
						return;
					}

					Point mouse = display.getCursorLocation();
					Rectangle displayarea = shell.getBounds();

					// Left/Right check
					int x = (mouse.x < displayarea.x) ? Integer.MIN_VALUE :
							(mouse.x > displayarea.x + displayarea.width) ? Integer.MAX_VALUE :
							0;

					// Up/Down check
					int y = (mouse.y < displayarea.y) ? Integer.MAX_VALUE :
							(mouse.y > displayarea.y + displayarea.height) ? Integer.MIN_VALUE :
							0;

					// Notify and repeat
					if (x != 0 || y != 0) {
						handler.mouseMove(x, y);
					}
					display.timerExec(20, this);
				}
			});
		}

		/**
		 * Mouse hovering event, does nothing.
		 * 
		 * @param mouseevent Details on the mouse event that occurred.
		 */
		public void mouseHover(MouseEvent mouseevent) {
		}

		/**
		 * Method for notifying the input inputhandler of mouse move events.
		 * 
		 * @param mouseevent Details on the mouse event that occurred.
		 */
		public void mouseMove(MouseEvent mouseevent) {

			handler.mouseMove(mouseevent.x - (width >> 1), (height >> 1) - mouseevent.y);
		}

		/**
		 * Method for notifying the input inputhandler of mouse up events.
		 * 
		 * @param mouseevent Details on the mouse event that occurred.
		 */
		public void mouseUp(MouseEvent mouseevent) {

			handler.mouseUp(mouseevent.button, mouseevent.x - (width >> 1), (height >> 1) - mouseevent.y);
		}
	}
}
