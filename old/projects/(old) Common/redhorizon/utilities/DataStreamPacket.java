
// =====================================
// Scanner's Java - Streamed data object
// =====================================

package redhorizon.utilities;

import java.nio.ByteBuffer;

/**
 * Representation of a 'packet' of data being pushed down a stream.  Can either
 * hold data, or indicate the end-of-stream with a <tt>null</tt> value.  Used to
 * get around the non-null restrictions of queue data structures.
 * 
 * @author Emanuel Rabina
 */
class DataStreamPacket {

	final ByteBuffer data;

	/**
	 * Constructor, determines the packet type from the given data.  Passing
	 * <tt>null</tt> is used to indicate an end-of-stream packet.
	 * 
	 * @param data <tt>ByteBuffer</tt> of data to store in this packet, or
	 * 			   <tt>null</tt> if this packet should indicate the end.
	 */
	DataStreamPacket(ByteBuffer data) {

		this.data = data;
	}
}
