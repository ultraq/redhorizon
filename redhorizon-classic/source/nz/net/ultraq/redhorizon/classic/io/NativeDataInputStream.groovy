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

package nz.net.ultraq.redhorizon.classic.io

import java.nio.ByteOrder

/**
 * A variant of {@link DataInputStream} that reads primitive types respecting
 * the native byte order of the underlying platform.
 *
 * @author Emanuel Rabina
 */
class NativeDataInputStream extends InputStream implements DataInput, NativeReader {

	@Delegate
	private final DataInputStream dis
	private final boolean isLittleEndian
	private int bytesRead
	private int markAt

	/**
	 * Constructor, wraps the given input stream so that values read from it are
	 * in native byte order.
	 */
	NativeDataInputStream(InputStream inputStream) {

		dis = new DataInputStream(inputStream)
		isLittleEndian = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN
	}

	/**
	 * Returns the number of bytes read in the stream so far.
	 */
	int getBytesRead() {

		return bytesRead
	}

	@Override
	synchronized void mark(int readLimit) {

		markAt = bytesRead
		dis.mark(readLimit)
	}

	@Override
	boolean markSupported() {

		return dis.markSupported()
	}

	@Override
	int read() {

		var result = dis.read()
		bytesRead++
		return result
	}

	/**
	 * Reads an {@code int} value, respecting the byte order of the underlying
	 * platform.
	 */
	@Override
	int readInt() {

		return isLittleEndian ? readLittleEndian(4) : dis.readInt()
	}

	/**
	 * Reads a {@code short} value, respecting the byte order of the underlying
	 * platform.
	 */
	@Override
	short readShort() {

		return isLittleEndian ? readLittleEndian(2) : dis.readShort()
	}

	/**
	 * Reads an unsigned {@code short} value, returning it as an {@code int} as
	 * that is the next primitive able to hold the unsigned value.
	 */
	int readUnsignedShort() {

		return readShort() & 0xffff
	}

	@Override
	synchronized void reset() {

		bytesRead = markAt
		dis.reset()
	}

	@Override
	long skip(long n) {

		var result = dis.skip(n)
		bytesRead += n
		return result
	}

	@Override
	int skipBytes(int n) {

		var result = dis.skipBytes(n)
		bytesRead += n
		return result
	}
}
