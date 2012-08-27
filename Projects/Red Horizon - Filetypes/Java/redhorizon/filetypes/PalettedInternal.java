
package redhorizon.filetypes;

import redhorizon.media.Palette;

import java.nio.channels.ReadableByteChannel;

/**
 * Interface to expose an image file's internal palette and raw (indexed) image
 * data.
 * 
 * @author Emanuel Rabina
 */
public interface PalettedInternal {

	/**
	 * Retrieves the internal palette.
	 * 
	 * @return An image file's internal palette.
	 */
	public Palette getPalette();

	/**
	 * Returns the raw indexed data which constructs this file's image.  The
	 * returned data is not a copy, and so any changes to the returned data will
	 * affect this image's data.
	 * 
	 * @return The indexed image data.
	 */
	public ReadableByteChannel getRawImageData();
}
