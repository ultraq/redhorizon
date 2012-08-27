package redhorizon.filetypes;

/**
 * Enumeration of the supported bitrates.
 * 
 * @author Emanuel Rabina
 */
public enum SoundBitrate {

	BITRATE_8(8),
	BITRATE_16(16);

	public final int size;

	/**
	 * Constructor, sets the number of bits per sound sample.
	 * 
	 * @param size
	 */
	private SoundBitrate(int size) {

		this.size = size;
	}
}
