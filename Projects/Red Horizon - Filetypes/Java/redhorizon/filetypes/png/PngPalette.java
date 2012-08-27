
package redhorizon.filetypes.png;

import redhorizon.filetypes.ColourFormat;
import redhorizon.filetypes.pal.PalFile;
import redhorizon.media.AbstractPalette;
import redhorizon.media.Palette;

/**
 * Palette internal to a png file.
 * 
 * @author Emanuel Rabina
 */
public class PngPalette extends AbstractPalette {

	/**
	 * Constructor, creates a png file internal palette.
	 * 
	 * @param format RGB(A) type of this palette.
	 * @param bytes Byte array containing the palette data.
	 */
	PngPalette(ColourFormat format, byte[][] bytes) {

		super(256, format, bytes);
	}

	/**
	 * Constructor, create a new palette from a VGA palette file.
	 * 
	 * @param palettefile
	 */
	PngPalette(PalFile palettefile) {

		super(palettefile);
	}

	/**
	 * Constructor, create a palette from another palette.
	 * 
	 * @param palette
	 */
	PngPalette(Palette palette) {

		super(palette);
	}
}
