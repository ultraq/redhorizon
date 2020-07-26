/* 
 * Copyright 2020, Emanuel Rabina (http://www.ultraq.net.nz/)
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
 * A variant of {@link DataOutputStream} that writes primitive types respecting
 * the native byte order of the underlying platform.
 * 
 * @author Emanuel Rabina
 */
class NativeDataOutputStream extends OutputStream implements DataOutput {

	@Delegate
	private final DataOutputStream dos
	private final boolean isLittleEndian

	/**
	 * Constructor, wraps the output stream so that values written to it are in
	 * native byte order.
	 * 
	 * @param outputStream
	 */
	NativeDataOutputStream(OutputStream outputStream) {

		dos = new DataOutputStream(outputStream)
		isLittleEndian = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN
	}

	/**
	 * Writes a {@code int} value, respecting the byte order of the underlying
	 * platform.
	 * 
	 * @param v
	 */
	@Override
	void writeInt(int v) {

		if (isLittleEndian) {
			write(v)
			write(v >>> 8)
			write(v >>> 16)
			write(v >>> 24)
		}
		else {
			dos.writeInt(v)
		}
	}

	/**
	 * Writes a {@code short} value, respecting the byte order of the underlying
	 * platform.
	 * 
	 * @param v
	 */
	@Override
	void writeShort(int v) {

		if (isLittleEndian) {
			write(v)
			write(v >>> 8)
		}
		else {
			dos.writeShort(v)
		}
	}
}
