
package redhorizon.filetypes;

import java.nio.channels.ReadableByteChannel;

/**
 * Interface for files that represent a colour palette.
 * 
 * @author Emanuel Rabina
 */
public interface PaletteFile extends File {

	/**
	 * Colour format used by this palette.
	 * 
	 * @return Palette colour format, RGB(A).
	 */
	public ColourFormat format();

	/**
	 * Returns a byte channel into the palette's data.
	 * 
	 * @return Palette data.
	 */
	public ReadableByteChannel getPaletteData();

	/**
	 * The number of colours in the palette.
	 * 
	 * @return Number of colours.
	 */
	public int size();
}
