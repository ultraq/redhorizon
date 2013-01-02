
// =================================
// Scanner's Java - Display callback
// =================================

package redhorizon.engine.display;

/**
 * Implementors of this interface are items that wish to be notified by the
 * {@link GameWindow} of when window/display events have occured.
 * 
 * @author Emanuel Rabina
 */
public interface DisplayCallback {

	/**
	 * Notification that the display area has been closed.  This can be
	 * considered synonymous with ending the rendering loop and finishing-up the
	 * game.
	 */
	public void displayClosed();

	/**
	 * Notification that there is now a visible rendering space being actively
	 * updated.
	 */
	public void displayInit();

	/**
	 * Notification that a new rendering cycle has started.
	 */
	public void displayRendering();
}
