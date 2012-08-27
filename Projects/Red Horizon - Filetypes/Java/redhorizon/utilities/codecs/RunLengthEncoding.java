
package redhorizon.utilities.codecs;

import java.nio.ByteBuffer;

/**
 * A basic, configurable, run-length decoder.
 *
 * @author Emanuel Rabina
 */
public class RunLengthEncoding implements Decoder {

	private final byte countbyte;

	/**
	 * Constructor, configures the run-length decoding to recognize the given
	 * byte as the 'count' byte.
	 * 
	 * @param countbyte
	 */
	public RunLengthEncoding(byte countbyte) {

		this.countbyte = countbyte;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void decode(ByteBuffer source, ByteBuffer dest, ByteBuffer... extra) {

		while (source.hasRemaining()) {
			byte value = source.get();

			// Count byte & copy byte run
			if ((value & countbyte) == countbyte) {
				int count = value & ~countbyte;
				byte copy = source.get();

				while (count-- > 0) {
					dest.put(copy);
				}
			}
			// Non-count byte
			else {
				dest.put(value);
			}
		}
		dest.rewind();
	}
}
