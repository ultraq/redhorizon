
package redhorizon.filetypes.bmp;

import redhorizon.filetypes.ColourFormat;
import redhorizon.filetypes.FileExtensions;
import redhorizon.filetypes.AbstractFile;
import redhorizon.filetypes.ImageFile;
import redhorizon.utilities.channels.ReadableByteChannelAdapter;
import static redhorizon.filetypes.ColourFormat.FORMAT_RGB;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.BufferedInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import javax.imageio.ImageIO;

/**
 * Wrapper of the Java implementation for bitmap images.
 * 
 * @author Emanuel Rabina
 */
@FileExtensions("bmp")
public class BmpFile extends AbstractFile implements ImageFile {

	private ByteBuffer imagedata;
	private int width;
	private int height;

	/**
	 * Constructor, creates a bitmap file with the given name and data.
	 * 
	 * @param name		  Name of the bitmap.
	 * @param bytechannel Source data of the bitmap.
	 */
	public BmpFile(String name, ReadableByteChannel bytechannel) {

		super(name);

		// Read the file data
		BufferedImage image = ImageIO.read(new BufferedInputStream(Channels.newInputStream(bytechannel)));

		// Fill bitmap attributes
		width  = image.getWidth();
		height = image.getHeight();

		// Switch R->B and B->R in BGR bitmaps
		byte[] bitmapbytes = ((DataBufferByte)image.getData().getDataBuffer()).getData();
		for (int i = 0; i < bitmapbytes.length; i += 3) {
			byte r2b = bitmapbytes[i];
			byte b2r = bitmapbytes[i + 2];
			bitmapbytes[i] = b2r;
			bitmapbytes[i + 2] = r2b;
		}

		// Save bitmap image
		imagedata = ByteBuffer.wrap(bitmapbytes);
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void close() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ColourFormat format() {

		return FORMAT_RGB;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReadableByteChannel getImageData() {

		return new ReadableByteChannelAdapter(imagedata);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int height() {

		return height;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int width() {

		return width;
	}
}
