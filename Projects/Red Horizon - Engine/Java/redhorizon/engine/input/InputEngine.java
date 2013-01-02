
// ========================================
// Scanner's Java - User interaction engine
// ========================================

package redhorizon.engine.input;

import redhorizon.engine.Engine;
import redhorizon.engine.display.GameWindow;
import redhorizon.engine.display.InputEventHandler;

import org.eclipse.swt.SWT;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A sub-section of the overall game engine, this class and thread runs and
 * handles the user input part of the game.  It's main task is to interpret
 * standard keyboard and mouse events into the more complicated commands desired
 * to interact with the game.
 * <p>
 * Input events require an application focus, otherwise they won't be received,
 * so it is up to the component of the game engine that creates the user
 * display to attach this class to the application through the
 * {@link GameWindow#setInputEventHandler(InputEventHandler)} method.
 * 
 * @author Emanuel Rabina
 */
public class InputEngine extends Engine<InputEngineCallback, InputEngineListener>
	implements InputEventHandler {

	// Time to wait between event handling
	private static long INPUT_ENGINE_BACKOFF_PERIOD = 20;

	private ConcurrentLinkedQueue<KeyEvent> keyeventqueue     = new ConcurrentLinkedQueue<KeyEvent>();
	private ConcurrentLinkedQueue<MouseEvent> mouseeventqueue = new ConcurrentLinkedQueue<MouseEvent>();
	private Controller controller;

	/**
	 * Constructor, initializes the input settings for the game.
	 * 
	 * @param callback The class to notify of any significant input events.
	 */
	public InputEngine(InputEngineCallback callback) {

		super(callback);
	}

	/**
	 * @inheritDoc
	 */
	protected long backoffPeriod() {

		return INPUT_ENGINE_BACKOFF_PERIOD;
	}

	/**
	 * Initializes the {@link Controller}; a representation of the user actions.
	 */
	@Override
	protected void doInit() {

		controller = new Controller();
		callback.initInput();
	}

	/**
	 * Handles the queued events accumulated through mouse/keyboard actions.
	 */
	@Override
	protected void doRun() {

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

	/**
	 * Does nothing for the input engine.
	 */
	@Override
	protected void doShutdown() {

	}

	/**
	 * Returns the current controller object.
	 * 
	 * @return The current controller.
	 */
	public Controller getCurrentController() {

		return controller;
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
}
