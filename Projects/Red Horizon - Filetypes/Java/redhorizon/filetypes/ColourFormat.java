
package redhorizon.filetypes;

/**
 * Enumeration of the supported colour formats.
 * 
 * @author Emanuel Rabina
 */
public enum ColourFormat {

	FORMAT_INDEXED(1),
	FORMAT_RGB(3),
	FORMAT_RGBA(4);

	public final int size;

	/**
	 * Constructor, sets the number of bytes per colour.
	 * 
	 * @param size
	 */
	private ColourFormat(int size) {

		this.size = size;
	}
}
