
package redhorizon.filetypes.wsa;

import redhorizon.media.AbstractPalette;
import redhorizon.media.Palette;
import static redhorizon.filetypes.ColourFormat.FORMAT_RGB;

import java.nio.ByteBuffer;

/**
 * Palette internal to a wsa file.
 * 
 * @author Emanuel Rabina
 */
public class WsaPalette extends AbstractPalette {

	/**
	 * Constructor, creates a wsa file internal palette.
	 * 
	 * @param bytes Buffer containing the palette data.
	 */
	WsaPalette(ByteBuffer bytes) {

		super(256, FORMAT_RGB, bytes);
	}

	/**
	 * Constructor, copy an existing palette.
	 * 
	 * @param palette
	 */
	WsaPalette(Palette palette) {

		super(palette);
	}
}
