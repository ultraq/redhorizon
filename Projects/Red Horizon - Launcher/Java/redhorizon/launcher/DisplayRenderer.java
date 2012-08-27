
package redhorizon.launcher;

/**
 * Interface to notify a graphics engine of rendering events triggered from the
 * window's rendering thread.
 * 
 * @author Emanuel Rabina
 */
public interface DisplayRenderer {

	/**
	 * Notification that rendering can begin.
	 */
	public void displayRendering();

	/**
	 * Notification that the OpenGL canvas is being closed.
	 */
	public void displayShutdown();

	/**
	 * Notification that the OpenGL canvas is being initialized.
	 */
	public void displayStartup();
}
