
// ============================
// Scanner's Java - Mouse event
// ============================

package redhorizon.engine.input;

/**
 * Class containing information on the mouse event, such as click type and
 * pointer location.
 * <p>
 * The modifier state/mask are the same as those used in the
 * <tt>org.eclipse.swt.SWT</tt> class.
 */
public class MouseEvent extends InputEvent<MouseAction> {

	final int button;
	final int x;
	final int y;

	/**
	 * Constructor, sets the mouse event's action and co-ordinates.
	 * 
	 * @param action The type of action this event represents.
	 * @param button The button that was used.
	 * @param x		 X co-ordinate of the mouse over the display area.
	 * @param y		 Y co-ordinate of the mouse over the display area.
	 */
	MouseEvent(MouseAction action, int button, int x, int y) {

		super(action);
		this.button = button;
		this.x      = x;
		this.y      = y;
	}
}
