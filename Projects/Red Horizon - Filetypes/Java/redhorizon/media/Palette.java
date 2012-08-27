
package redhorizon.media;

import redhorizon.filetypes.ColourFormat;

/**
 * Interface for colour palettes.
 * 
 * @author Emanuel Rabina
 */
public interface Palette {

	/**
	 * Colour format used by this palette.
	 * 
	 * @return Palette colour format, RGB(A).
	 */
	public ColourFormat format();

	/**
	 * Return the colour data at the specified index.
	 * 
	 * @param index Position in the palette.
	 * @return <tt>byte</tt> array of the RGB(A) values of the requested colour.
	 */
	public byte[] getColour(int index);

	/**
	 * The number of colours in the palette.
	 * 
	 * @return Number of colours.
	 */
	public int size();
}
