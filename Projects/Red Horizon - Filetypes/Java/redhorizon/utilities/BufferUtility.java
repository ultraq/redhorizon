
package redhorizon.utilities;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;

/**
 * Utility class for common buffer operations.
 * 
 * @author Emanuel Rabina.
 */
public class BufferUtility {

	/**
	 * Hidden default constructor, as this class is only ever meant to be used
	 * statically.
	 */
	private BufferUtility() {
	}

	/**
	 * Read a byte channel until all input is exhausted, returning the results
	 * in a byte buffer.
	 * 
	 * @param bytechannel
	 * @return All remaining data in the stream.
	 */
	public static ByteBuffer readRemaining(ReadableByteChannel bytechannel) {

		ArrayList<ByteBuffer> bytes = new ArrayList<>();
		int size = 0;
		while (true) {
			ByteBuffer bytedata = ByteBuffer.allocate(1024);
			int read = bytechannel.read(bytedata);
			if (read > 0) {
				size += read;
				bytes.add((ByteBuffer)bytedata.flip());
			}
			if (read != 1024) {
				break;
			}
		}

		ByteBuffer data = ByteBuffer.allocate(size);
		for (ByteBuffer bytedata: bytes) {
			data.put(bytedata);
		}
		data.rewind();
		return data;
	}
}
