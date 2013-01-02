
// =========================================
// Scanner's Java - Playable (sound) objects
// =========================================

package redhorizon.scenegraph;

import redhorizon.engine.audio.AudioObject;

/**
 * Interface defining methods for objects that can be heard by the player.
 * Primarily, they have control over whether to have these objects played at the
 * next rendering pass.
 * 
 * @author Emanuel Rabina
 */
public interface Playable extends AudioObject {

	/**
	 * Queries the state of the playable, whether or not it's playing.
	 * 
	 * @return <tt>true</tt> if the sound is still playing, <tt>false</tt>
	 * 		   otherwise.
	 */
	public boolean isPlaying();

	/**
	 * Requests that the object be played at the next audio rendering pass
	 * onwards.
	 */
	public void play();

	/**
	 * Requests that the object stop any sounds that are currently playing.
	 */
	public void stop();
}
