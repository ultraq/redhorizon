
package redhorizon.filetypes.mix;

import java.nio.ByteBuffer;

/**
 * Representation of a Red Alert MIX file header, which contains information
 * about the mix file and it's contents.
 * 
 * @author Emanuel Rabina
 */
public class MixFileHeader {

	static final int HEADER_SIZE = 6;

	final short numfiles;
	final int bodylength;

	/**
	 * Constructor, takes a <tt>ByteBuffer</tt> and assigns the bytes to the
	 * variables of the header.
	 * 
	 * @param bytes A wrapped array of bytes which consists of the header.
	 */
	MixFileHeader(ByteBuffer bytes) {

		numfiles   = bytes.getShort();
		bodylength = bytes.getInt();
	}
}
