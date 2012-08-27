
package redhorizon.filetypes.pcx;

import redhorizon.media.AbstractPalette;

import static redhorizon.filetypes.ColourFormat.FORMAT_RGB;

import java.nio.ByteBuffer;

/**
 * Palette internal to a pcx file.
 * 
 * @author Emanuel Rabina
 */
public class PcxPalette extends AbstractPalette {

	/**
	 * Constructor, creates a pcx file internal palette.
	 * 
	 * @param bytes Buffer containing the palette data.
	 */
	PcxPalette(ByteBuffer bytes) {

		super(256, FORMAT_RGB, bytes);
	}
}
