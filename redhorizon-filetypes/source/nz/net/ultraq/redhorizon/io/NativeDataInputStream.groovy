/*
 * Copyright 2019, Emanuel Rabina (http://www.ultraq.net.nz/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nz.net.ultraq.redhorizon.io

import java.nio.ByteOrder

/**
 * A variant of {@link DataInputStream} that reads primitive types respecting
 * the native byte order of the underlying platform.
 * 
 * @author Emanuel Rabina
 */
class NativeDataInputStream extends InputStream implements DataInput {

	@Delegate
	private final DataInputStream dis
	private final boolean isLittleEndian

	NativeDataInputStream(InputStream inputStream) {
		dis = new DataInputStream(inputStream)
		isLittleEndian = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN
	}

	/**
	 * Reads an {@code int} value, respecting the byte order of the underlying
	 * platform.
	 * 
	 * @param self
	 * @return
	 */
	@Override
	int readInt() {
		return isLittleEndian ? readLittleEndian(4) : dis.readInt()
	}

	/**
	 * Read the given number of bytes in little endian byte order, returning the
	 * expected primitive that comprises those bytes.
	 * 
	 * @param numBytes
	 * @return
	 */
	private long readLittleEndian(int numBytes) {
		long result = 0
		for (def i = 0; i < numBytes; i++) {
			def b = read()
			if (b < 0) {
				throw new EOFException()
			}
			result += b << (8 * i)
		}
		return result
	}

	/**
	 * Reads a {@code short} value, respecting the byte order of the underlying
	 * platform.
	 * 
	 * @param self
	 * @return
	 */
	@Override
	short readShort() {
		return isLittleEndian ? readLittleEndian(2) : dis.readShort()
	}
}
