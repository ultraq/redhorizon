
// ====================================
// Scanner's Java - Input event inputhandler
// ====================================

package redhorizon.engine.display;

/**
 * Interface for a class to be notified of, and deal with, input events such as
 * mouse clicks, mouse movement, and keyboard presses.  Mouse events passed from
 * the {@link GameWindow} are expected to be in the form of world co-ordinates.
 * That is:<br>
 *  - the center of the display area is (0,0)<br>
 *  - the top is (0, +y)<br>
 *  - the bottom is (0, -y)<br>
 * (in widgets, the y-axis is often the other way around)<br>
 * <br>
 * The keyboard modifiers state/mask should be similar to those defined in the
 * <code>org.eclipse.swt.SWT</code> class.
 * 
 * @author Emanuel Rabina
 */
public interface InputEventHandler {

	/**
	 * Activated when a keyboard key has been pressed.
	 * 
	 * @param character <code>char</code> representation of the key that was
	 * 					pressed.
	 * @param keycode	Keycode of the key that was typed.  Used for
	 * 					non-character keys such as the arrow keys, etc.
	 * @param modifiers The current state of any keyboard modifiers.
	 */
	public void keyPressed(char character, int keycode, int modifiers);

	/**
	 * Activated when a keyboard modifier (CTRL, ALT, etc) has been released.
	 * 
	 * @param modifiers The current state of any keyboard modifiers.
	 */
	public void keyReleased(int modifiers);

	/**
	 * Activated when a mouse button has been double-clicked.
	 * 
	 * @param button The button that was clicked.
	 * @param xcoord X co-ordinate where the click was made.
	 * @param ycoord Y co-ordinate where the click was made.
	 */
	public void mouseDoubleClick(int button, int xcoord, int ycoord);

	/**
	 * Activated when a mouse button has been clicked down.
	 * 
	 * @param button The button that was clicked.
	 * @param xcoord X co-ordinate where the click was made.
	 * @param ycoord Y co-ordinate where the click was made.
	 */
	public void mouseDown(int button, int xcoord, int ycoord);

	/**
	 * Activated when the mouse moves around the OpenGL rendering area. X and Y
	 * co-ordinates of {@link Integer#MIN_VALUE} or {@link Integer#MAX_VALUE}
	 * are to be interpreted as scrolling events in their respective
	 * directions.
	 * 
	 * @param xcoord X co-ordinate where the mouse currently is.
	 * @param ycoord Y co-ordinate where the mouse currently is.
	 */
	public void mouseMove(int xcoord, int ycoord);

	/**
	 * Activated when a mouse button has been clicked up.
	 * 
	 * @param button The button that was clicked.
	 * @param xcoord X co-ordinate where the click was made.
	 * @param ycoord Y co-ordinate where the click was made.
	 */
	public void mouseUp(int button, int xcoord, int ycoord);
}
