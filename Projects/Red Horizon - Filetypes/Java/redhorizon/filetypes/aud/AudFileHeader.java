
package redhorizon.filetypes.aud;

import java.nio.ByteBuffer;

/**
 * Representation of the header section of an AUD file, which contains data on
 * the AUD file and it's contents.
 * 
 * @author Emanuel Rabina
 */
public class AudFileHeader {

	static final int HEADER_SIZE = 12;

	final short frequency;
	final int filesize;
	final int datasize;
	final byte flags;
	final byte type;

	/**
	 * Constructor, assigns the variables of the header using the bytes from the
	 * given {@link ByteBuffer}.
	 * 
	 * @param bytes Aud file header data.
	 */
	AudFileHeader(ByteBuffer bytes) {

		frequency = bytes.getShort();
		filesize  = bytes.getInt();
		datasize  = bytes.getInt();
		flags     = bytes.get();
		type      = bytes.get();
	}
}
