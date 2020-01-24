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

package nz.net.ultraq.redhorizon.extensions

import java.nio.ByteBuffer

/**
 * Instance method extensions to the {@code ByteBuffer} class.
 * 
 * @author Emanuel Rabina
 */
class ByteBufferExtensions {

	/**
	 * Advance the internal position of a buffer by the given amount.  Negative
	 * values may be given to move the position backwards.
	 * 
	 * @param self
	 * @param n
	 * @return The buffer.
	 */
	static ByteBuffer advance(ByteBuffer self, int n) {

		return self.position(self.position() + n)
	}

	/**
	 * A relative bulk <i>put</i> method using another {@code ByteBuffer} but only
	 * reading up to {@code length} bytes from that buffer.  The position of both
	 * buffers will be increased by {@code length}.
	 * 
	 * @param self
	 * @param src
	 * @param length
	 * @return
	 */
	static ByteBuffer put(ByteBuffer self, ByteBuffer src, int length) {

		self.put(src.array(), src.position(), length)
		src.advance(length)
		return self
	}

	/**
	 * Split a buffer into several smaller buffers of the specified size.  If the
	 * size doesn't cleanly divide into the current buffer, then the final buffer
	 * will be smaller and contain the remaining bytes.
	 * 
	 * @param self
	 * @param length
	 * @return
	 */
	static ByteBuffer[] split(ByteBuffer self, int length) {

		self.rewind()
		def buffers = new ByteBuffer[Math.ceil(self.limit() / length)].collect {
			def bufferSize = Math.min(length, self.remaining())
			return ByteBuffer.allocate(bufferSize).order(self.order())
				.put(self, bufferSize)
				.rewind()
		}
		self.rewind()
		return buffers
	}
}
