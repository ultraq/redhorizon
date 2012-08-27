
package redhorizon.filetypes.wsa;

import java.nio.ByteBuffer;

/**
 * Representation of a WSA file's header.  Much like the SHP file, it is just
 * basic information on the contents of the file.
 * 
 * @author Emanuel Rabina
 */
public class WsaFileHeaderCNC extends WsaFileHeader {

	static final int HEADER_SIZE = 14;

	final short x, y;
	final short width, height;
	final int delta;

	/**
	 * Constructor, generates header data from the given <tt>ByteBuffer</tt>.
	 * 
	 * @param bytes <tt>ByteBuffer</tt> containing WSA header data.
	 */
	WsaFileHeaderCNC(ByteBuffer bytes) {

		super(bytes.getShort());
		x      = bytes.getShort();
		y      = bytes.getShort();
		width  = bytes.getShort();
		height = bytes.getShort();
		delta  = bytes.getInt();
	}

	/**
	 * Constructor, uses the given parameters to complete this header.
	 * 
	 * @param numframes Number of frames in the file.
	 * @param x			X offset to position animation.
	 * @param y			Y offset to position animation.
	 * @param width		Width of each frame.
	 * @param height	Height of each frame.
	 * @param delta		Animation delta.
	 */
	WsaFileHeaderCNC(short numframes, short x, short y, short width, short height, int delta) {

		super(numframes);
		this.x      = x;
		this.y      = y;
		this.width  = width;
		this.height = height;
		this.delta  = delta;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	ByteBuffer toByteBuffer() {

		ByteBuffer header = ByteBuffer.allocate(HEADER_SIZE);
		header.putShort(numframes);
		header.putShort(x);
		header.putShort(y);
		header.putShort(width);
		header.putShort(height);
		header.putInt(delta);
		header.rewind();
		return header;
	}
}
