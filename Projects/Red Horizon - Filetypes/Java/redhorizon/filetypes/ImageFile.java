
package redhorizon.filetypes;

import java.nio.channels.ReadableByteChannel;

/**
 * Interface for image file formats.
 * 
 * @author Emanuel Rabina
 */
public interface ImageFile extends ImageCommon, File {

	/**
	 * Returns a byte channel into the image data of the file.
	 * 
	 * @return Byte channel containing the bytes in RGB(A) order for the image.
	 */
	public ReadableByteChannel getImageData();
}
