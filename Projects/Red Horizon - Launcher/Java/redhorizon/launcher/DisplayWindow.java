
package redhorizon.launcher;

/**
 * Main display window.
 * 
 * @author Emanuel Rabina
 */
public abstract class DisplayWindow extends Window {

	protected final DisplayRenderer renderer;

	/**
	 * Constructor, set the renderer associated with this window.
	 * 
	 * @param renderer Listener for the rendering event.
	 */
	protected DisplayWindow(DisplayRenderer renderer) {

		this.renderer = renderer;
	}
}
