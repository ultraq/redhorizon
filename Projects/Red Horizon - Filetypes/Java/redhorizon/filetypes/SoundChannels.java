
package redhorizon.filetypes;

/**
 * Enumeration of the supported channel types.
 * 
 * @author Emanuel Rabina
 */
public enum SoundChannels {

	CHANNELS_MONO(1),
	CHANNELS_STEREO(2);

	public final int size;

	/**
	 * Constructor, sets the number of channels per track.
	 * 
	 * @param size
	 */
	private SoundChannels(int size) {

		this.size = size;
	}
}
