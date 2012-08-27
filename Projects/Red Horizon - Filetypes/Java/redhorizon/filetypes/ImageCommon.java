
package redhorizon.filetypes;

/**
 * Interface containing parts common to all of the image file types in this
 * package.
 * 
 * @author Emanuel Rabina
 */
interface ImageCommon {

	/**
	 * Returns the number of bytes used to represent the colour data of a single
	 * pixel.
	 * <p>
	 * If the object implements the {@link Paletted} interface, then the return
	 * value of this method is more of an expectation of the colour-depth,
	 * rather than a given.
	 * 
	 * @return The image colour format.
	 */
	public ColourFormat format();

	/**
	 * Returns the height of the image.
	 * 
	 * @return Height of the image.
	 */
	public int height();

	/**
	 * Returns the width of the image.
	 * 
	 * @return Width of the image.
	 */
	public int width();
}
