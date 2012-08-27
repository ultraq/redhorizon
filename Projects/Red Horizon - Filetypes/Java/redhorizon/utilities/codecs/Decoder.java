
package redhorizon.utilities.codecs;

import java.nio.ByteBuffer;

/**
 * Interface for classes that can decode information.
 *
 * @author Emanuel Rabina
 */
public interface Decoder {

	/**
	 * Decodes the information in the <tt>source</tt> buffer, putting it into
	 * the <tt>dest</tt> buffer.
	 * 
	 * @param source
	 * @param dest
	 * @param extra	 Optional buffers to pass to the decoder.  Used in some
	 * 				 decoders that require multiple source buffers to produce a
	 * 				 decoded result (eg: XOR'ed images).
	 */
	public void decode(ByteBuffer source, ByteBuffer dest, ByteBuffer... extra);
}
