
// ======================================
// Scanner's Java - Video events listener
// ======================================

package redhorizon.media;

/**
 * Interface defining events that might be of interest to external objects,
 * namely the notification that a video has finished the entire track.
 * 
 * @author Emanuel Rabina
 */
public interface VideoListener extends AnimationListener, SoundTrackListener {

	/**
	 * Called when a video has finished all frames / all soundtrack.
	 */
	public void finished();
}
