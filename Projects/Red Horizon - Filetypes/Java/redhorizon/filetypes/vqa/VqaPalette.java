
package redhorizon.filetypes.vqa;

import redhorizon.media.AbstractPalette;
import static redhorizon.filetypes.ColourFormat.FORMAT_RGB;

import java.nio.ByteBuffer;

/**
 * Dynamic palette found within a vqa file.
 * 
 * @author Emanuel Rabina
 */
public class VqaPalette extends AbstractPalette {

	/**
	 * Constructor, builds this palette.
	 * 
	 * @param size
	 * @param bytes Buffer containing the palette data.
	 */
	VqaPalette(int size, ByteBuffer bytes) {

		super(size, FORMAT_RGB, bytes);
	}
}
