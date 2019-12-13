
// ================================================
// Scanner's Java - Image stretched over the screen
// ================================================

package redhorizon.media;

import redhorizon.engine.Camera;
import redhorizon.filetypes.ImageFile;
import redhorizon.filetypes.ImagesFile;
import redhorizon.filetypes.Paletted;
import redhorizon.misc.geometry.Rectangle2D;
import redhorizon.resources.ResourceManager;

import java.nio.ByteBuffer;

/**
 * An image type that represents a single image scaled to fit into the screen
 * while maintaining aspect ratio.
 * 
 * @author Emanuel Rabina.
 */
public class ImageScaled extends Image {

	/**
	 * Constructor, creates an image out of the given image file data.
	 * 
	 * @param imagefile The file containing the image to make.
	 */
	public ImageScaled(ImageFile imagefile) {

		this(imagefile.filename(), imagefile.format(), imagefile.width(), imagefile.height(),
				(imagefile instanceof Paletted) ? ((Paletted)imagefile).applyPalette(ResourceManager.getPalette())[0]:
				imagefile.getImage(), (imagefile instanceof Paletted));
	}

	/**
	 * Constructor, creates an image from an image inside a file containing
	 * several images.
	 * 
	 * @param imagesfile The file containing several images, including the one
	 * 					 to make.
	 * @param imagenum	 The 0-index image number to use for this object.
	 */
	public ImageScaled(ImagesFile imagesfile, int imagenum) {

		this(imagesfile.filename() + "_" + imagenum, imagesfile.format(), imagesfile.width(), imagesfile.height(),
				(imagesfile instanceof Paletted) ? ((Paletted)imagesfile).applyPalette(ResourceManager.getPalette())[imagenum]:
				imagesfile.getImages()[imagenum], (imagesfile instanceof Paletted));
	}

	/**
	 * Constructor, creates an image using the given image parts.
	 * 
	 * @param name	   The name of the image.
	 * @param format   RGB/A format of the image.
	 * @param width	   The width of the image.
	 * @param height   The height of the image.
	 * @param image	   The bytes consisting of the image.
	 * @param paletted Whether the file is paletted or not.
	 */
	ImageScaled(String name, int format, int width, int height, ByteBuffer image, boolean paletted) {

		super(name, format, width, height, image, paletted);

		// Alter widths and heights to fit screen and maintain aspect ratio
		Rectangle2D viewarea = Camera.currentCamera().getProjectionVolume();
		float viewwidth  = viewarea.width();
		float viewheight = viewarea.height();
		float aspectratio = viewwidth / viewheight;
		float modifier = ((float)width / (float)height >= aspectratio) ?
				viewwidth / width:
				viewheight / height;

		coords = coords.scale(modifier);
	}
}
