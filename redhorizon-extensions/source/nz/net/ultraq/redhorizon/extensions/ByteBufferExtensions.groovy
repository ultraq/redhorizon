/* 
 * Copyright 2016, Emanuel Rabina (http://www.ultraq.net.nz/)
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
import java.nio.ByteOrder

/**
 * Extensions to the {@code ByteBuffer} class.
 * 
 * @author Emanuel Rabina
 */
class ByteBufferExtensions {

	/**
	 * Allocates a direct {@code ByteBuffer} with native byte ordering.
	 * 
	 * @param self
	 * @param capacity
	 * @return Natively ordered {@code ByteBuffer}.
	 */
	static ByteBuffer allocateDirectNative(ByteBuffer self, int capacity) {

		def buffer = ByteBuffer.allocateDirect(capacity)
		buffer.order(ByteOrder.nativeOrder())
		return buffer
	}

	/**
	 * Allocates a {@code ByteBuffer} with native byte ordering.
	 * 
	 * @param self
	 * @param capacity
	 * @return Natively ordered {@code ByteBuffer}.
	 */
	static ByteBuffer allocateNative(ByteBuffer self, int capacity) {

		def buffer = ByteBuffer.allocate(capacity)
		buffer.order(ByteOrder.nativeOrder())
		return buffer
	}

	/**
	 * Wraps a {@code byte[]} in a {@code ByteBuffer} with native byte ordering.
	 * 
	 * @param self
	 * @param array
	 * @return Natively ordered {@code ByteBuffer}.
	 */
	static ByteBuffer wrapNative(ByteBuffer self, byte[] array) {

		def buffer = ByteBuffer.wrap(array)
		buffer.order(ByteOrder.nativeOrder())
		return buffer
	}

	/**
	 * Wraps a {@code byte[]} in a {@code ByteBuffer} with native byte ordering.
	 * 
	 * @param self
	 * @param array
	 * @param offset
	 * @param length
	 * @return Natively ordered {@code ByteBuffer}.
	 */
	static ByteBuffer wrapNative(ByteBuffer self, byte[] array, int offset, int length) {

		def buffer = ByteBuffer.wrap(array, offset, length)
		buffer.order(ByteOrder.nativeOrder())
		return buffer
	}
}
