
package redhorizon.filetypes.wsa;

import java.nio.ByteBuffer;

/**
 * Abstract WSA file header class containing only the parts similar between both
 * C&C WSA file and Dune 2 WSA file headers.
 * 
 * @author Emanuel Rabina
 */
public abstract class WsaFileHeader {

	final short numframes;

	/**
	 * Constructor, fills-out the common number-of-frames and offsets part.
	 * 
	 * @param numframes The number of frames the WSA file contains.
	 */
	WsaFileHeader(short numframes) {

		this.numframes = numframes;
	}

	/**
	 * Returns this header in it's {@link ByteBuffer} representation.
	 * 
	 * @return {@link ByteBuffer} containing this header's data.
	 */
	abstract ByteBuffer toByteBuffer();
}
