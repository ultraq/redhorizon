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
 * A read-only variant of {@link RandomAccessFile} that reads primitive types
 * respecting the native byte order of the underlying platform.
 *
 * @author Emanuel Rabina
 */
class NativeRandomAccessFile implements Closeable, DataInput, NativeReader {

	@Delegate
	private final RandomAccessFile raf
	private final boolean isLittleEndian

	/**
	 * Constructor, opens the file as a {@link RandomAccessFile} in read mode, but
	 * reads primitive values from it in native byte order.
	 */
	NativeRandomAccessFile(File file) {

		raf = new RandomAccessFile(file, 'r')
		isLittleEndian = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN
	}

	/**
	 * Reads an {@code int} value, respecting the byte order of the underlying
	 * platform.
	 */
	@Override
	int readInt() {

		return isLittleEndian ? readLittleEndian(4) : raf.readInt()
	}

	/**
	 * Reads a {@code short} value, respecting the byte order of the underlying
	 * platform.
	 */
	@Override
	short readShort() {

		return isLittleEndian ? readLittleEndian(2) : raf.readShort()
	}
}
