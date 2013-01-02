
// ==============================================
// Scanner's Java - Handle to buffered sound data
// ==============================================

package redhorizon.media;

import redhorizon.filetypes.SoundFile;

import net.java.games.joal.AL;
import static net.java.games.joal.AL.*;

import java.nio.ByteBuffer;

/**
 * Class which contains handles and details of sound data obtained from a sound
 * file (those which implement the {@link SoundFile} interface).  In obtaining
 * a fully-fledged sound to use in-game, this class is much like an intermediary
 * step between the file and the {@link SoundEffect} class.  Because playable
 * sounds (sources) are obtained from pre-existing cached sounds (buffers), this
 * class acts as those buffers.
 * 
 * @author Emanuel Rabina
 */
class SoundData {

	private final int bufferid;

	/**
	 * Constructor, stores the data, from these raw components of audio data,
	 * into a buffer to be used by sources (<code>Playable</code> objects) in
	 * the program.
	 * 
	 * @param al		Current OpenAL pipeline.
	 * @param data		<code>ByteBuffer</code> containing the sound data.
	 * @param bitrate	Bits per sample of the sound.
	 * @param channels	Number of sound channels.
	 * @param frequency	Frequency at which the sound is sampled.
	 */
	SoundData(AL al, ByteBuffer data, int bitrate, int channels, int frequency) {

		// Discover the format of the sound
		int format = bitrate == 8 ?
				(channels == 1 ? AL_FORMAT_MONO8  : AL_FORMAT_STEREO8) :
				(channels == 1 ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16);

		// Get a buffer ID
		int[] bufferids = new int[1];
		al.alGenBuffers(1, bufferids, 0);
		bufferid = bufferids[0];

		// Fill the buffer with audio data
		al.alBufferData(bufferid, format, data, data.limit(), frequency);
	}

	/**
	 * Deletes the buffer from memory.
	 * 
	 * @param al Current OpenAL pipeline.
	 */
	void delete(AL al) {

		al.alDeleteBuffers(1, new int[]{ bufferid }, 0);
	}

	/**
	 * Returns the handle to the buffer containing the audio data.
	 * 
	 * @return The sound's buffer ID.
	 */
	int getBufferID() {

		return bufferid;
	}
}
