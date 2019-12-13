
// ===============================
// Scanner's Java - Keyboard event
// ===============================

package redhorizon.engine.input;

/**
 * Keyboard input event type.
 * 
 * @author Emanuel Rabina
 */
/**
 * Class to contain information on a keyboard event, such as the character
 * pressed, as well as any modifiers currently being used.
 * <p>
 * The modifier state/mask are the same as those used in the
 * <tt>org.eclipse.swt.SWT</tt> class.
 */
public class KeyEvent extends InputEvent<KeyAction> {

	final char character;
	final int keycode;
	final int modifiers;

	/**
	 * Constructor, sets this keyboard event's pressed key and the current
	 * keyboard modifiers state.
	 * 
	 * @param action	The type of action this event represents.
	 * @param character The character representing the key being pressed.
	 * @param keycode	Number code for the key being pressed when it is not
	 * 					a character key.
	 * @param modifiers Mask used to identify the current state of any
	 * 					keyboard modifers.
	 */
	KeyEvent(KeyAction action, char character, int keycode, int modifiers) {

		super(action);
		this.character = character;
		this.keycode   = keycode;
		this.modifiers = modifiers;
	}
}
