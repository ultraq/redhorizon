
package redhorizon.filetypes.aud;

import java.nio.ByteBuffer;

/**
 * Representation of a chunk header in an AUD file.
 * 
 * @author Emanuel Rabina.
 */
public class AudChunkHeader {

//	private static final int CHUNK_ID = 0x0000deaf;

	static final int CHUNK_HEADER_SIZE = 8;

	// Chunk header data
	final short filesize;
	final short datasize;
	final int id;

	/**
	 * Constructor, assigns the variables of this chunk with the given bytes.
	 * 
	 * @param bytes Aud file data.
	 */
	AudChunkHeader(ByteBuffer bytes) {

		filesize = bytes.getShort();
		datasize = bytes.getShort();
		id       = bytes.getInt();
	}
}
