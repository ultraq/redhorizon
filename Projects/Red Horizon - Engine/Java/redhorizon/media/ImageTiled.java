
// =========================================
// Scanner's Java - Tiled/Repeating 2D image
// =========================================

package redhorizon.media;

import redhorizon.filetypes.ImageFile;
import redhorizon.filetypes.ImagesFile;
import redhorizon.filetypes.Paletted;
import redhorizon.resources.ResourceManager;

import javax.media.opengl.GL;

import java.nio.ByteBuffer;

/**
 * Similar to it's parent class, the <code>ImageTiled</code> is used for
 * images which repeat over a specified area.  Both the number of repeats, as
 * well as the area it does it over, are required in the constructors.
 * 
 * @author Emanuel Rabina
 */
public class ImageTiled extends Image {

	private final float repeatx;
	private final float repeaty;

	/**
	 * Constructor, creates an image from a specified file with the given
	 * dimensions, overriding those of the source image.
	 * 
	 * @param imagefile	Name of the file which contains an image to construct
	 * 					this object out of.
	 * @param repeatx	Number of times to have the image repeat horizontally.
	 * @param repeaty	Number of times to have the image repeat vertically.
	 */
	public ImageTiled(ImageFile imagefile, float repeatx, float repeaty) {

		this(imagefile.filename(), imagefile.format(), imagefile.width(), imagefile.height(),
				imagefile instanceof Paletted ? ((Paletted)imagefile).applyPalette(ResourceManager.getPalette())[0]:
				imagefile.getImage(), (imagefile instanceof Paletted), repeatx, repeaty);
	}

	/**
	 * Constructor, creates an image object from a specified image within a file
	 * that contains many, of the dimensions specified.
	 * 
	 * @param imagesfile Name of the file which contains an image to construct
	 * 					 this object out of.
	 * @param imagenum	 The image number within the file (0-index).
	 * @param repeatx	 Number of times to have the image repeat horizontally.
	 * @param repeaty	 Number of times to have the image repeat vertically.
	 */
	public ImageTiled(ImagesFile imagesfile, int imagenum, float repeatx, float repeaty) {

		this(imagesfile.filename(), imagesfile.format(), imagesfile.width(), imagesfile.height(),
				imagesfile instanceof Paletted ? ((Paletted)imagesfile).applyPalette(ResourceManager.getPalette())[imagenum]:
				imagesfile.getImages()[imagenum], (imagesfile instanceof Paletted), repeatx, repeaty);
	}

	/**
	 * Constructor, creates an image out of the given image parts.
	 * 
	 * @param name	   The name of the image.
	 * @param format   RGB/A format of the image.
	 * @param width	   The width of the image.
	 * @param height   The height of the image.
	 * @param image	   The bytes consisting of the image.
	 * @param paletted Whether the file is paletted or not.
	 * @param repeatx  Number of times to have the image repeat horizontally
	 * 				   over the <code>wraparea</code>.
	 * @param repeaty  Number of times to have the image repeat vertically over
	 * 				   the <code>wraparea</code>.
	 */
	public ImageTiled(String name, int format, int width, int height, ByteBuffer image, boolean paletted,
			float repeatx, float repeaty) {

		super(name, format, width, height, image, paletted);

		// Set repeat draw parameters
		coords = coords.scale(repeatx, repeaty);
		this.repeatx = repeatx;
		this.repeaty = repeaty;
	}

	/**
	 * @inheritDoc
	 */
	public void init(GL gl) {

		// See if the image has already been loaded, otherwise create the image/texture
		ImageData imagedata = ResourceManager.getImageData(name);
		if (imagedata == null) {
			imagedata = new ImageData(gl, image, format, texturewidth, textureheight, true);
			ResourceManager.storeImageData(name, imagedata);
		}
		textureid   = imagedata.getTextureID();
		widthratio  = imagedata.getWidthRatio()  * repeatx;
		heightratio = imagedata.getHeightRatio() * repeaty;

		// Drop the buffer
		image = null;
	}
}
