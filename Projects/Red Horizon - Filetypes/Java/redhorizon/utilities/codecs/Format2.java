
package redhorizon.utilities.codecs;

import java.nio.ByteBuffer;

/**
 * Encoder/decoder utilizing the Format2 compression scheme.
 * <p>
 * Format2 is a very simple compression algorithm used by Dune 2.  It works much
 * like run-length-encoding schemes, but only for series' of 0s.  Non-zero
 * values are written verbatim.  A Format2-encoded file can be decoded as
 * follows:
 * <ol>
 *   <li>0 c = Fill the next c bytes with 0<li>
 *   <li>v   = Write v<li>
 * </ol>
 * 
 * @author Emanuel Rabina
 */
public class Format2 implements Encoder, Decoder {

	private static final byte CMD_FILL     = 0;
	private static final byte CMD_FILL_VAL = 0;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void decode(ByteBuffer source, ByteBuffer dest, ByteBuffer... extra) {

		while (source.hasRemaining()) {
			byte command = source.get();

			// Fill 0s
			if (command == CMD_FILL) {
				int count = source.get() & 0xff;
				while (count-- > 0) {
					dest.put(CMD_FILL_VAL);
				}
			}
			// Write direct value
			else {
				dest.put(command);
			}
		}
		dest.flip();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void encode(ByteBuffer source, ByteBuffer dest, ByteBuffer... extra) {

		int count = 0;
		int limit = Math.min(source.limit(), 255);

		outer: while (source.hasRemaining()) {
			byte value = source.get();

			// Count a series of 0s, describe the series
			while (value == CMD_FILL_VAL) {
				while (value == CMD_FILL_VAL && count < limit) {
					count++;
					if (source.hasRemaining()) {
						value = source.get();
					}
					else {
						break;
					}
				}
				dest.put(new byte[]{ CMD_FILL, (byte)count });
				count = 0;
				if (!source.hasRemaining()) {
					break outer;
				}
			}

			// Write non-0 value
			dest.put(value);
		}
		source.rewind();
		dest.flip();
	}
}
