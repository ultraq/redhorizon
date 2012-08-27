
package redhorizon.launcher;

/**
 * Abstract class to provide no-op implementations of the window listener
 * methods.
 * 
 * @author Emanuel Rabina
 */
public abstract class WindowAdaptor implements WindowListener {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void windowClose() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void windowOpen() {
	}
}
