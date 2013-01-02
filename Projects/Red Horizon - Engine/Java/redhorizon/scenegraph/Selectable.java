
// =================================================
// Scanner's Java - An object selectable by the user
// =================================================

package redhorizon.scenegraph;

/**
 * Interface for game objects that are selectable from the game world via the
 * user interface, and as such can response to user interface events.
 * 
 * @author Emanuel Rabina
 */
public interface Selectable {

	/**
	 * Notification that this object has been deselected by the user.
	 */
	public void deselect();

	/**
	 * Notification that this object has been selected by the user.
	 */
	public void select();
}
