
package redhorizon.filetypes.vqa;

import java.nio.ByteBuffer;

/**
 * Representation of the VQA file's offset data.
 * 
 * @author Emanuel Rabina
 */
public class VqaFrameOffset {

	static final int FRAME_OFFSET_SIZE = 4;

	final int offset;

	/**
	 * Constructor, creates an offset record from the given {@link ByteBuffer}.
	 * 
	 * @param bytes {@link ByteBuffer} to a VQA offset record.
	 */
	VqaFrameOffset(ByteBuffer bytes) {

		offset = (bytes.getInt() & 0x3fffffff) << 1;
	}
}
