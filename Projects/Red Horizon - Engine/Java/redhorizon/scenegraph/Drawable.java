
// =========================================
// Scanner's Java - Drawable (image) objects
// =========================================

package redhorizon.scenegraph;

import redhorizon.engine.graphics.GraphicsObject;

/**
 * Interface to define methods for objects that can be drawn onto the
 * screen.  Primarily, it's control over whether to have these objects drawn at
 * the next rendering pass.
 * 
 * @author Emanuel Rabina
 */
public interface Drawable extends GraphicsObject {

	/**
	 * Requests that the object be drawn from the next rendering pass onwards.
	 */
	public void draw();

	/**
	 * Requests that the object stop being drawn from the next rendering pass
	 * onwards.
	 */
	public void erase();

	/**
	 * Queries whether this item is being drawn.
	 * 
	 * @return <tt>true</tt> if it is to be drawn, <tt>false</tt> otherwise.
	 */
	public boolean isDrawing();
}
