
// ==========================================
// Scanner's Java - Animation events listener
// ==========================================

package redhorizon.media;

/**
 * Interface defining events that might be of interest to external objects,
 * namely the notification that an animation has completed it's cycle.
 * 
 * @author Emanuel Rabina
 */
public interface AnimationListener {

	/**
	 * Called when an animation has gone through all of it's frames of
	 * animation.
	 */
	public void finished();
}
