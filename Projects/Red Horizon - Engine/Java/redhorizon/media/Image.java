
package redhorizon.media;

import redhorizon.filetypes.ImageFile;
import redhorizon.filetypes.ImagesFile;
import redhorizon.filetypes.Paletted;
import redhorizon.resources.ResourceManager;

import javax.media.opengl.GL;
import static javax.media.opengl.GL.*;

import java.nio.ByteBuffer;

/**
 * Parent class for all image types, contains the most basic methods for images
 * used throughout the game.
 * 
 * @author Emanuel Rabina
 */
public class Image extends Media implements Drawable {

	// Image adjustment constants
	public static final float OPACITY_MIN = 0.0f;
	public static final float OPACITY_MAX = 1.0f;

	// Image file attributes
	final int format;
	Rectangle2D coords;
	ByteBuffer image;

	int textureid;
	int texturewidth;
	int textureheight;
	float widthratio;
	float heightratio;

	// Image adjustment
	boolean drawing;
	float opacity = OPACITY_MAX;

	/**
	 * Constructor, creates an image out of the given image file data.
	 * 
	 * @param imagefile The file containing the image to make.
	 */
	public Image(ImageFile imagefile) {

		this(imagefile.filename(), imagefile.format(), imagefile.width(), imagefile.height(),
			(imagefile instanceof Paletted) ? ((Paletted)imagefile).applyPalette(ResourceManager.getPalette())[0]:
			imagefile.getImage(), (imagefile instanceof Paletted));
	}

	/**
	 * Constructor, creates an image from a file containing several images.
	 * 
	 * @param imagesfile The file containing several images, including the one
	 * 					 to make.
	 * @param imagenum	 The 0-index image number to use for this object.
	 */
	public Image(ImagesFile imagesfile, int imagenum) {

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
	Image(String name, int format, int width, int height, ByteBuffer image, boolean paletted) {

		super(name + (paletted ? "_" + ResourceManager.getPalette() : ""));
		this.format = format;
		this.coords = new Rectangle2D(-width >> 1, -height >> 1, width >> 1, height >> 1);
		this.image  = image;

		texturewidth  = width;
		textureheight = height;
	}

	/**
	 * @inheritDoc
	 */
	public void delete(GL gl) {

		// Do nothing
	}

	/**
	 * @inheritDoc
	 */
	public void draw() {

		drawing = true;
	}

	/**
	 * @inheritDoc
	 */
	public void erase() {

		drawing = false;
	}

	/**
	 * @inheritDoc
	 */
	public Rectangle2D getBoundingArea() {

		return coords;
	}

	/**
	 * Returns the level of opacity currently applied to this drawable.  Opacity
	 * is a floating-point value, between the range of {@link #OPACITY_MIN} and
	 * {@link #OPACITY_MAX}.
	 * 
	 * @return Node's level of opacity.
	 */
	public float getOpacity() {

		return opacity;
	}

	/**
	 * @inheritDoc
	 */
	public void init(GL gl) {

		// See if the image has already been loaded, otherwise create the image/texture
		ImageData imagedata = ResourceManager.getImageData(name);
		if (imagedata == null) {
			imagedata = new ImageData(gl, image, format, texturewidth, textureheight);
			ResourceManager.storeImageData(name, imagedata);
		}
		textureid = imagedata.getTextureID();
		widthratio = imagedata.getWidthRatio();
		heightratio = imagedata.getHeightRatio();

		// Drop the buffer
		image = null;
	}

	/**
	 * @inheritDoc
	 */
	public boolean isDrawing() {

		return drawing;
	}

	/**
	 * @inheritDoc
	 * 
	 * TODO: Optimize drawing code w/ vertex arrays, buffer objects, etc
	 */
	public void render(GL gl) {

		// Select and draw the texture
		gl.glBindTexture(GL_TEXTURE_2D, textureid);
		gl.glColor4f(1, 1, 1, opacity);
		gl.glBegin(GL_QUADS);
		{
			gl.glTexCoord2f(0, 0);						gl.glVertex2f(coords.getLeft(), coords.getTop());
			gl.glTexCoord2f(0, heightratio);			gl.glVertex2f(coords.getLeft(), coords.getBottom());
			gl.glTexCoord2f(widthratio, heightratio);	gl.glVertex2f(coords.getRight(), coords.getBottom());
			gl.glTexCoord2f(widthratio, 0);				gl.glVertex2f(coords.getRight(), coords.getTop());
		}
		gl.glEnd();
	}

	/**
	 * Sets the opacity/transparency of this node.  Opacity is a floating-point
	 * value between the range of {@link #OPACITY_MIN} and {@link #OPACITY_MAX}.
	 * Any values passed to this method beyond these values, will be clamped.
	 * 
	 * @param opacity Level of opacity to apply to this node.
	 */
	public void setOpacity(float opacity) {

		this.opacity = Math.min(Math.max(opacity, OPACITY_MIN), OPACITY_MAX);
	}
}
