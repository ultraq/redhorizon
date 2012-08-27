
package redhorizon.utilities;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Ensures that every {@link ByteBuffer} ever created is made with native byte
 * ordering.
 *
 * @author Emanuel Rabina
 */
public aspect NativeByteOrderer {

	/**
	 * After the construction of a byte buffer, set it to have native byte
	 * ordering.
	 * 
	 * @return ByteBuffer with native ordering.
	 */
	ByteBuffer around():
		call(public static ByteBuffer ByteBuffer.allocate*(..)) ||
		call(public static ByteBuffer ByteBuffer.wrap(..)) {

		ByteBuffer buffer = proceed();
		buffer.order(ByteOrder.nativeOrder());
		return buffer;
	}
}
