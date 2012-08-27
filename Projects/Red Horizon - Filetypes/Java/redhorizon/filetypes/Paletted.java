
package redhorizon.filetypes;

/**
 * Interface which identifies the file format as using indexed data to represent
 * the image information returned by the implementing class.  Thus requiring a
 * matching external palette to obtain the whole image.
 * <p>
 * A class that implements this interface redefines the meaning of any methods
 * that retrieve the images (eg: <tt>get*()</tt> methods) as those will now just
 * return the raw indexed image data.
 * <p>
 * A file format which contains an internal palette is not considered
 * <tt>Paletted</tt>.
 * 
 * @author Emanuel Rabina
 */
public interface Paletted extends ImageCommon {

}
