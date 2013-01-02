
// ====================================
// Scanner's Java - Timed event handler
// ====================================

package redhorizon.utilities;

/**
 * Timed event handler interface to handle a single timed tasks.
 * 
 * @author Emanuel Rabina
 */
public interface TimerTask {

	/**
	 * Called when a task is started.
	 */
	public void begin();
}
