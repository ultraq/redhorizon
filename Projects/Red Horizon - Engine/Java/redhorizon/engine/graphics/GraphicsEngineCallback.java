
// ==========================================================
// Scanner's Java - Response methods to display engine events
// ==========================================================

package redhorizon.engine.graphics;

import redhorizon.engine.EngineCallback;

/**
 * A brief interface defining methods that the graphics engine can use to notify
 * the main game engine that significant graphics events are taking place which
 * the game engine should respond to.
 * 
 * @author Emanuel Rabina
 */
public interface GraphicsEngineCallback extends EngineCallback {

	/**
	 * Notification that the display has been initialized, and so any items
	 * requiring a current display should be loaded.
	 */
	public void initRendering();

	/**
	 * Notification that the display has been closed, and so action by the
	 * callback should be taken.
	 */
	public void stopRendering();
}
