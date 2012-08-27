
package redhorizon.filetypes.shp;

import java.nio.ByteBuffer;

/**
 * Representation of a SHP file's header format.
 * 
 * @author Emanuel Rabina
 */
public class ShpFileHeaderCNC extends ShpFileHeader {

	static final int HEADER_SIZE = 14;

	final short unknown1;
	final short unknown2;
	final short width;
	final short height;
	final int unknown3;

	/**
	 * Constructor, obtains image parameters from the <tt>ByteBuffer</tt>.
	 * 
	 * @param bytes <tt>ByteBuffer</tt> containing SHP file header data.
	 */
	ShpFileHeaderCNC(ByteBuffer bytes) {

		super(bytes.getShort());

		unknown1 = bytes.getShort();
		unknown2 = bytes.getShort();
		width    = bytes.getShort();
		height   = bytes.getShort();
		unknown3 = bytes.getInt();
	}

	/**
	 * Constructor, uses the given parameters to fill-in this header.
	 * 
	 * @param numimages Number of images in the SHP file.
	 * @param width		Width of each image in the file.
	 * @param height	Height of each image in the file.
	 */
	ShpFileHeaderCNC(short numimages, short width, short height) {

		super(numimages);

		this.width  = width;
		this.height = height;

		unknown1 = 0;
		unknown2 = 0;
		unknown3 = 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	ByteBuffer toByteBuffer() {

		ByteBuffer header = ByteBuffer.allocate(HEADER_SIZE);
		header.putShort(numimages)
			  .putShort(unknown1)
			  .putShort(unknown2)
			  .putShort(width)
			  .putShort(height)
			  .putInt(unknown3)
			  .rewind();

		return header;
	}
}
