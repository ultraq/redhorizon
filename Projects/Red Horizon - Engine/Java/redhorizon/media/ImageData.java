
// ==================================
// Scanner's Java - Image data handle
// ==================================

package redhorizon.media;

import redhorizon.engine.Capabilities;
import redhorizon.filetypes.ImageFile;
import redhorizon.filetypes.ImagesFile;
import redhorizon.utilities.BufferUtility;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import static javax.media.opengl.GL.*;

import java.nio.ByteBuffer;

/**
 * Class which contains handles and details about an image loaded from an image
 * file (those which implement the {@link ImageFile} or {@link ImagesFile}
 * interfaces).  This class is much like an intermediary step between between an
 * image file and the {@link Image} class.<br>
 * <br>
 * Although there is no OpenGL equivalent of OpenAL's buffers/sources model,
 * this class exists to maintain the same sort of model of reusable media.
 * 
 * @author Emanuel Rabina
 */
class ImageData {

	// System is capable of non-power-of-two textures?
	private static final boolean npotcapable = Capabilities.includes("GL_ARB_texture_non_power_of_two");

	private final int textureid;
	private final float widthratio;
	private final float heightratio;

	/**
	 * Constructor, creates a new <code>ImageData</code> tailored for the
	 * system's graphics capabilities.  By default the generated texture does
	 * not repeat/tile.
	 * 
	 * @param gl	 Current OpenGL pipeline.
	 * @param data	 <code>ByteBuffer</code> containing the image data.
	 * @param format Internal format (RGB/A) of the image.
	 * @param width	 Width of the image.
	 * @param height Height of the image.
	 */
	ImageData(GL gl, ByteBuffer data, int format, int width, int height) {

		this(gl, data, format, width, height, false);
	}

	/**
	 * Constructor, creates a new <code>ImageData</code> tailored for the
	 * system's graphics capabilities.
	 * 
	 * @param gl	 Current OpenGL pipeline.
	 * @param data	 <code>ByteBuffer</code> containing the image data.
	 * @param format Internal format (RGB/A) of the image.
	 * @param width	 Width of the image.
	 * @param height Height of the image.
	 * @param repeat Allow the generated texture to be repeated/tiled.
	 */
	ImageData(GL gl, ByteBuffer data, int format, int width, int height, boolean repeat) {

		// Generate a texture ID
		int[] textureids = new int[1];
		gl.glGenTextures(1, textureids, 0);
		textureid = textureids[0];

		// External format type
		int xformat = format == 3 ? GL_RGB : GL_RGBA;

		// Load a texture to that texture ID
		gl.glBindTexture(GL_TEXTURE_2D, textureid);

		// Set texture parameters
		if (!repeat) {
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		}
		else {
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		}

		// Use videocard's non-power-of-two texture abilities
		if (npotcapable) {
			gl.glTexImage2D(GL_TEXTURE_2D, 0, format, width, height, 0, xformat, GL_UNSIGNED_BYTE, data);
			widthratio  = 1f;
			heightratio = 1f;
		}
		else {

			// Use mipmaps for repeating textures
			if (repeat) {
				new GLU().gluBuild2DMipmaps(GL_TEXTURE_2D, format, width, height, xformat, GL_UNSIGNED_BYTE, data);
				widthratio  = 1f;
				heightratio = 1f;
			}

			// Generate a power-of-two texture
			else {
				int width2  = nextPowerOfTwo(width);
				int height2 = nextPowerOfTwo(height);
				int potlength = width * format;
				int npotlength = width2 * format;
				byte[] potimage  = new byte[npotlength * height2];

				for (int y = 0; y < height; y++) {
					data.get(potimage, y * npotlength, potlength);
				}

				ByteBuffer potdata = BufferUtility.wrapByteBuffer(potimage);
				gl.glTexImage2D(GL_TEXTURE_2D, 0, format, width2, height2, 0, xformat, GL_UNSIGNED_BYTE, potdata);
				widthratio  = (float)width  / (float)width2;
				heightratio = (float)height / (float)height2;
			}
		}
	}

	/**
	 * Deletes the texture from memory.
	 * 
	 * @param gl Current OpenGL pipeline.
	 */
	void delete(GL gl) {

		gl.glDeleteTextures(1, new int[]{ textureid }, 0);
	}

	/**
	 * Returns the height ratio of the image to the texture.  Used for non-
	 * 'non-power-of-two' capable video cards.
	 * 
	 * @return Image height to texture height ratio.
	 */
	float getHeightRatio() {

		return heightratio;
	}

	/**
	 * Returns the handle to the texture containing the image data.
	 * 
	 * @return The image's texture ID.
	 */
	int getTextureID() {

		return textureid;
	}

	/**
	 * Returns the width ratio of the image to the texture.  Used for non-
	 * 'non-power-of-two' capable video cards.
	 * 
	 * @return Image width to texture width ratio.
	 */
	float getWidthRatio() {

		return widthratio;
	}

	/**
	 * Finds-out the smallest power-of-two which is larger than the given value.
	 * 
	 * @param value Value to use for discovery.
	 * @return Smallest power-of-two integer which is larger than
	 * 		   <code>value</code>.
	 */
	private static int nextPowerOfTwo(int value) {

		int pot = 2;
		while (pot < value) {
			pot *= 2;
		}
		return pot;
	}
}
