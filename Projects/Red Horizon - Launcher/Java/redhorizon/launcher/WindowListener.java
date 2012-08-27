
package redhorizon.launcher;

/**
 * Interface for receiving notification of window events.
 * 
 * @author Emanuel Rabina
 */
public interface WindowListener {

	/**
	 * Notification that the window is about to close.
	 */
	public void windowClose();

	/**
	 * Notification that the window has been opened.
	 */
	public void windowOpen();
}
