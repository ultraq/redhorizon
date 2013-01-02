
// =================================================
// Scanner's Java - Response methods to input events
// =================================================

package redhorizon.engine.input;

import redhorizon.engine.EngineCallback;

/**
 * Interface which the input engine uses to notify the main game engine of input
 * engine events.
 * 
 * @author Emanuel Rabina
 */
public interface InputEngineCallback extends EngineCallback {

	/**
	 * Notification that the input engine has been initialized.
	 */
	public void initInput();

	/**
	 * Notification that the input engine has been stopped.
	 */
	public void stopInput();
}
