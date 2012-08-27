
package redhorizon.filetypes.cps;

import redhorizon.media.AbstractPalette;
import redhorizon.media.Palette;

import static redhorizon.filetypes.ColourFormat.*;

import java.nio.ByteBuffer;

/**
 * Palette internal to a cps file.
 * 
 * @author Emanuel Rabina
 */
public class CpsPalette extends AbstractPalette {

	/**
	 * Constructor, creates a cps file internal palette.
	 * 
	 * @param bytes Buffer containing the palette data.
	 */
	CpsPalette(ByteBuffer bytes) {

		super(256, FORMAT_RGB, bytes);
	}

	/**
	 * Constructor, copy an existing palette.
	 * 
	 * @param palette
	 */
	CpsPalette(Palette palette) {

		super(palette);
	}
}
