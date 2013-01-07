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

package redhorizon.engine.input;

import redhorizon.engine.SubsystemCallback;
import redhorizon.engine.display.GameWindow;
import redhorizon.engine.display.InputEventDelegate;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Input subsystem, deals with the handling and dispatch of events from the
 * display window.
 * 
 * @author Emanuel Rabina
 */
public class InputSubsystem implements InputEventDelegate, Runnable {

	private final GameWindow window;
	private final SubsystemCallback callback;

	private ConcurrentLinkedQueue<KeyEvent> keyeventqueue     = new ConcurrentLinkedQueue<>();
	private ConcurrentLinkedQueue<MouseEvent> mouseeventqueue = new ConcurrentLinkedQueue<>();

	/**
	 * Constructor, initializes the input subsystem for the game.
	 * 
	 * @param window
	 * @param callback
	 */
	public InputSubsystem(GameWindow window, SubsystemCallback callback) {

		this.window   = window;
		this.callback = callback;
	}

	/**
	 * @inheritDoc
	 */
	public void keyPressed(char character, int keycode, int modifiers) {

		keyeventqueue.add(new KeyEvent(KeyAction.KEY_PRESSED, character, keycode, modifiers));
	}

	/**
	 * @inheritDoc
	 */
	public void keyReleased(int modifiers) {

		keyeventqueue.add(new KeyEvent(KeyAction.KEY_RELEASE, (char)0, 0, modifiers));
	}

	/**
	 * @inheritDoc
	 */
	public void mouseDoubleClick(int button, int xcoord, int ycoord) {

		mouseeventqueue.add(new MouseEvent(MouseAction.MOUSE_DBLCLICK, button, xcoord, ycoord));
	}

	/**
	 * @inheritDoc
	 */
	public void mouseDown(int button, int xcoord, int ycoord) {

		mouseeventqueue.add(new MouseEvent(MouseAction.MOUSE_DOWN, button, xcoord, ycoord));
	}

	/**
	 * @inheritDoc
	 */
	public void mouseMove(int xcoord, int ycoord) {

		mouseeventqueue.add(new MouseEvent(MouseAction.MOUSE_MOVE, -1, xcoord, ycoord));
	}

	/**
	 * @inheritDoc
	 */
	public void mouseUp(int button, int xcoord, int ycoord) {

		mouseeventqueue.add(new MouseEvent(MouseAction.MOUSE_UP, button, xcoord, ycoord));
	}

	/**
	 * Input handling loop, collects and dispatches input events to the
	 * appropriate handlers.
	 */
	@Override
	public void run() {

		Thread.currentThread().setName("Red Horizon - Input subsystem");

		try {
			// Startup
			callback.subsystemInit();

			// Input event handling
			// Keyboard events
			for (Iterator<KeyEvent> keyevents = keyeventqueue.iterator(); keyevents.hasNext(); ) {
				KeyEvent keyevent = keyevents.next();
				keyevents.remove();

				switch (keyevent.keycode) {

					// Skip current video
					// NOTE: Need a generic 'abort last action' event here
					case SWT.ESC:
						controller.skipVideo();
						break;

					// Player movement controls
					case SWT.ARROW_UP:
						controller.moveUp();
						break;
					case SWT.ARROW_DOWN:
						controller.moveDown();
						break;
					case SWT.ARROW_LEFT:
						controller.moveLeft();
						break;
					case SWT.ARROW_RIGHT:
						controller.moveRight();
						break;
				}
			}

			// Mouse events
			for (Iterator<MouseEvent> mouseevents = mouseeventqueue.iterator(); mouseevents.hasNext(); ) {
				MouseEvent mouseevent = mouseevents.next();
				mouseevents.remove();

				switch (mouseevent.action) {
				case MOUSE_DOWN:
					controller.mouseClickDown(mouseevent.x, mouseevent.y);
					break;
				case MOUSE_UP:
					controller.mouseClickUp();
					break;
				case MOUSE_MOVE:

					// Check for left/right scrolling
					if (mouseevent.x == Integer.MIN_VALUE) {
						controller.moveLeft();
					}
					else if (mouseevent.x == Integer.MAX_VALUE) {
						controller.moveRight();
					}

					// Check for up/down scrolling
					if (mouseevent.y == Integer.MIN_VALUE) {
						controller.moveDown();
					}
					else if (mouseevent.y == Integer.MAX_VALUE) {
						controller.moveUp();
					}

					// Normal on-screen movement
					controller.mouseMove(mouseevent.x, mouseevent.y);
					break;
				}
			}
		}
		finally {
			// Shutdown
			callback.subsystemStop();
		}
	}

	/**
	 * Inner-class for handling keyboard events on the game window.
	 */
	private class SWTKeyListener implements KeyListener {

		private final InputEventDelegate handler;

		/**
		 * Constructor, assigns the keyboard handler.
		 * 
		 * @param handler Keyboard event handler.
		 */
		private SWTKeyListener(InputEventDelegate handler) {

			this.handler = handler;
		}

		/**
		 * Method for taking user keyboard presses and translating them into events
		 * for the registered {@link InputEventDelegate} to deal with.
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

		private final InputEventDelegate handler;

		/**
		 * Constructor, assigns the mouse handler.
		 * 
		 * @param handler Mouse event handler.
		 */
		private SWTMouseListener(InputEventDelegate handler) {

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
