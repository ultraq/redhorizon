
// ============================
// Scanner's Java - Input event
// ============================

package redhorizon.engine.input;

/**
 * Parent class for all input event types.
 * 
 * @author Emanuel Rabina
 * @param <A> Input action enumeration.
 */
public abstract class InputEvent<A> {

	final A action;

	/**
	 * Package-visible constructor, sets the event action.
	 * 
	 * @param action The event action.
	 */
	InputEvent(A action) {

		this.action = action;
	}
}
