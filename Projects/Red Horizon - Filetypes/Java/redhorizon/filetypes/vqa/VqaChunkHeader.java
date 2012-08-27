
package redhorizon.filetypes.vqa;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Representation of the header part of a data chunk in a VQA file.  Each chunk
 * is headed by a 4-letter chunkname, then the length of that chunk in
 * big-endian order.
 * 
 * @author Emanuel Rabina
 */
public class VqaChunkHeader {

	static final int CHUNK_SIZE = 8;

	private static final String CHUNK_UNCOMPRESSED = "0";
//	private static final String CHUNK_COMPRESSED   = "Z";

	final char[] chunkname;
	final int length;

	/**
	 * Constructor, takes the chunk data from the given <tt>ByteBuffer</tt>.
	 * 
	 * @param bytes <tt>ByteBuffer</tt> containing the next chunk of data.
	 */
	VqaChunkHeader(ByteBuffer bytes) {

		bytes.limit(4);
		chunkname  = Charset.defaultCharset().decode(bytes).array();
		bytes.limit(bytes.capacity());
		length     = Integer.reverseBytes(bytes.getInt());
	}

	/**
	 * Returns the matching enum used to identify this chunk from it's name
	 * string.
	 * 
	 * @return Matching {@link VqaChunkTypes} for this chunk's name.
	 */
	VqaChunkTypes chunkType() {

		return VqaChunkTypes.getMatchingType(chunkname.toString());
	}

	/**
	 * Returns whether or not this chunk header represents compressed data that
	 * follows.
	 * 
	 * @return <tt>true</tt> if the following data is compressed, <tt>false</tt>
	 * 		   otherwise.
	 */
	boolean isCompressed() {

		return !chunkname.toString().endsWith(CHUNK_UNCOMPRESSED);
	}
}
