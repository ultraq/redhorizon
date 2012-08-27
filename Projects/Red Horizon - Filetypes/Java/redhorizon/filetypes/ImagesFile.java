
package redhorizon.filetypes;

import java.nio.channels.ReadableByteChannel;

/**
 * Interface for files which contain multiple images.  These form the basis for
 * animation filetypes, as well as files which just contain several images to
 * represent a whole (eg: map tiles).
 * 
 * @author Emanuel Rabina
 */
public interface ImagesFile extends ImageCommon, File {

	/**
	 * Returns the image data for all of the images in this file.
	 * 
	 * @return Image data for each image.
	 */
	public ReadableByteChannel getImagesData();

	/**
	 * Returns the number of images in this file.
	 *
	 * @return Number of images.
	 */
	public int numImages();
}
