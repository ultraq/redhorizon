
// ===================================
// Scanner's Java - Basic sound effect
// ===================================

package redhorizon.media;

import redhorizon.filetypes.SoundFile;
import redhorizon.geometry.Rectangle2D;
import redhorizon.geometry.Vector3D;
import redhorizon.resources.ResourceManager;
import redhorizon.scenegraph.Playable;

import net.java.games.joal.AL;
import static net.java.games.joal.AL.*;

import java.nio.ByteBuffer;

/**
 * Basic sound in a 3D space.  Sounds are constructed from an instance of
 * {@link SoundData}, and then the data and any handles to that data are fed
 * into this class.  This class then maintains it's own set of attributes which
 * can transform the original sound when it's played.
 * 
 * @author Emanuel Rabina
 */
public class SoundEffect extends Media implements Playable {

	// PlayableItem defaults
	Vector3D direction = new Vector3D(0,0,0);
	Vector3D velocity  = new Vector3D(0,0,0);
	boolean playing;
	boolean playdelay;

	// Sound file attributes
	final int bitrate;
	final int channels;
	final int frequency;
	ByteBuffer sound;

	// Sound parts
	int sourceid;
	int bufferid;

	/**
	 * Constructor, loads the sound from the given <code>SoundFile</code>.
	 * 
	 * @param soundfile File which should be used to construct this sound.
	 */
	public SoundEffect(SoundFile soundfile) {

		super(soundfile.filename());
		bitrate   = soundfile.bitrate();
		channels  = soundfile.channels();
		frequency = soundfile.frequency();
		sound     = soundfile.getSound();
	}

	/**
	 * @inheritDoc
	 */
	public void delete(AL al) {

		// (Stop and) Delete the source if this sound has a handle on it
		if (al.alIsSource(sourceid)) {

			// Complete stop if still playing
			if (playdelay) {
				al.alSourceStop(sourceid);
				stop0();
			}
			al.alDeleteSources(1, new int[]{ sourceid }, 0);
		}
	}

	/**
	 * @inheritDoc
	 */
	protected Rectangle2D getBoundingArea() {

		return Rectangle2D.DEFAULT;
	}

	/**
	 * Returns the direction in which this sound is travelling.
	 * 
	 * @return The direction as an XYZ vector.
	 */
	public Vector3D getDirection() {

		return direction;
	}

	/**
	 * Returns the velocity at which this sound is travelling.
	 * 
	 * @return The velocity of the sound as an XYZ vector.
	 */
	public Vector3D getVelocity() {

		return velocity;
	}

	/**
	 * @inheritDoc
	 */
	public void init(AL al) {

		// Load the appropriate sound data from the store, or create a new one
		SoundData sounddata = ResourceManager.getSoundData(name);
		if (sounddata == null) {
			sounddata = new SoundData(al, sound, bitrate, channels, frequency);
			ResourceManager.storeSoundData(name, sounddata);
		}
		bufferid = sounddata.getBufferID();

		// Drop the sound bytes
		sound = null;
	}

	/**
	 * Queries the state of the playable, whether or not it's playing.
	 * 
	 * @return <code>true</code> if the sound is still playing,
	 * 		   <code>false</code> otherwise.
	 */
	public boolean isPlaying() {

		return playing || playdelay;
	}

	/**
	 * @inheritDoc
	 */
	public void play() {

		playing = true;
	}

	/**
	 * Sets the play delay flag to <code>true</code>.
	 */
	void play0() {

		playdelay = true;
	}

	/**
	 * @inheritDoc
	 */
	public void render(AL al) {

		if (playdelay) {

			// Stop playing the sound effect, release the source
			if (!playing) {
				al.alSourceStop(sourceid);
				al.alDeleteSources(1, new int[]{ sourceid }, 0);
				stop0();
			}

			// Check if the sound effect has stopped by itself
			else {
				int[] state = new int[1];
				al.alGetSourcei(sourceid, AL_SOURCE_STATE, state, 0);
				if (state[0] == AL_STOPPED) {
					stop();
				}
			}
		}
		else {

			// Initial play of the sound effect
			if (playing) {

				// Generate a source if it doesn't already have one
				if (!al.alIsSource(sourceid)) {
					int[] sourceids = new int[1];
					al.alGenSources(1, sourceids, 0);
					sourceid = sourceids[0];
				}

				// Attach buffer to source
				al.alSourcei(sourceid, AL_BUFFER, bufferid);

				// Setup source attributes
				al.alSourceiv(sourceid, AL_POSITION, getAbsolutePosition().toArray(), 0);
				al.alSourcefv(sourceid, AL_DIRECTION, direction.toArray(), 0);
				al.alSourcefv(sourceid, AL_VELOCITY, velocity.toArray(), 0);

				// Play
				al.alSourcePlay(sourceid);
				play0();
			}
		}
	}

	/**
	 * Sets the direction in which this sound is travelling.
	 * 
	 * @param direction New XYZ vector for this sound's direction.
	 */
	public void setDirection(Vector3D direction) {

		this.direction = direction;
	}

	/**
	 * Sets the velocity at which this sound is travelling.
	 * 
	 * @param velocity New XYZ vector for this sound's velocity.
	 */
	public void setVelocity(Vector3D velocity) {

		this.velocity = velocity;
	}

	/**
	 * @inheritDoc
	 */
	public void stop() {

		playing = false;
	}

	/**
	 * Sets the play delay flag to <code>false</code>.
	 */
	void stop0() {

		playdelay = false;
	}
}
