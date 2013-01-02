
// =======================================
// Scanner's Java - Animated event handler
// =======================================

package redhorizon.utilities;

/**
 * Timed event handler interface to handle multiple timed tasks at specific
 * points in that task's execution.
 * 
 * @author Emanuel Rabina
 */
public interface AnimatorTask extends TimerTask {

	/**
	 * Called when an animation begins.
	 */
	public void begin();

	/**
	 * Called when a animation is complete.
	 */
	public void end();

	/**
	 * Called at regular intervals, by the {@link Animator}, notifies the
	 * handler implementation of how far along the animation has progressed.
	 * 
	 * @param fraction The fraction of the current animation cycle that has been
	 * 				   completed.
	 */
	public void event(float fraction);
}
