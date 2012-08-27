
package redhorizon.filetypes;

import java.nio.channels.ReadableByteChannel;

/**
 * Interface for filetypes for sound data (eg: AUD, WAV, etc...).
 * 
 * @author Emanuel Rabina
 */
public interface SoundFile extends File {

	/**
	 * Returns the bitrate of the sound sample.
	 * 
	 * @return One of 8 or 16 bits.
	 */
	public SoundBitrate bitrate();

	/**
	 * Returns the channels used by the sound.
	 * 
	 * @return One of mono or stereo.
	 */
	public SoundChannels channels();

	/**
	 * Returns the frequency (number of samples/second) of the sound.
	 * 
	 * @return The frequency of the sound.
	 */
	public int frequency();

	/**
	 * Returns a byte channel to the sound data in the file.
	 * 
	 * @return Byte channel containin the sound data.
	 */
	public ReadableByteChannel getSoundData();
}
