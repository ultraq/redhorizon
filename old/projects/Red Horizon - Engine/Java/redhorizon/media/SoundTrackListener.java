
// ===========================================
// Scanner's Java - SoundTrack events listener
// ===========================================

package redhorizon.media;

/**
 * Interface defining events that might be of interest to external objects,
 * namely the notification that a soundtrack has finished the entire track.
 * 
 * @author Emanuel Rabina
 */
public interface SoundTrackListener {

	/**
	 * Called when a soundtrack has completed the whole track.
	 */
	public void finished();
}
