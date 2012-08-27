
package redhorizon.utilities.codecs;

import java.nio.ByteBuffer;

/**
 * Interface for classes that can encode information.
 *
 * @author Emanuel Rabina
 */
public interface Encoder {

	/**
	 * Encodes the information in the <tt>source</tt> buffer, putting it into
	 * the <tt>dest</tt> buffer.
	 * 
	 * @param source
	 * @param dest
	 * @param extra	 Optional buffers to pass to the encoder.  Used in some
	 * 				 encoders that require multiple source buffers to produce an
	 * 				 encoded result (eg: XOR'ed images).
	 */
	public void encode(ByteBuffer source, ByteBuffer dest, ByteBuffer... extra);
}
