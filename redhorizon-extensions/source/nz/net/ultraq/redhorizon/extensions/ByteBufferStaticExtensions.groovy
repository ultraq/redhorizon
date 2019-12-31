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
 * Static method extensions to the {@code ByteBuffer} class.
 * 
 * @author Emanuel Rabina
 */
class ByteBufferStaticExtensions {

	/**
	 * Allocates a direct {@code ByteBuffer} with native byte ordering.
	 * 
	 * @param self
	 * @param capacity
	 * @return Natively ordered {@code ByteBuffer}.
	 */
	static ByteBuffer allocateDirectNative(ByteBuffer self, int capacity) {

		return ByteBuffer
			.allocateDirect(capacity)
			.order(ByteOrder.nativeOrder())
	}

	/**
	 * Allocates a {@code ByteBuffer} with native byte ordering.
	 * 
	 * @param self
	 * @param capacity
	 * @return Natively ordered {@code ByteBuffer}.
	 */
	static ByteBuffer allocateNative(ByteBuffer self, int capacity) {

		return ByteBuffer
			.allocate(capacity)
			.order(ByteOrder.nativeOrder())
	}

	/**
	 * Allocate a native-ordered {@code ByteBuffer}, with data from several other
	 * {@code ByteBuffer}s.
	 * 
	 * @param self
	 * @param buffers
	 * @return
	 */
	static ByteBuffer fromBuffers(ByteBuffer self, ByteBuffer... buffers) {

		return buffers
			.inject(ByteBuffer.allocateNative(buffers.sum { it.limit() })) { acc, b -> acc.put(b) }
			.rewind()
	}

	/**
	 * Allocate a direct, native-ordered {@code ByteBuffer}, with data from
	 * one or more {@code ByteBuffer}s.
	 * 
	 * @param self
	 * @param buffers
	 * @return
	 */
	static ByteBuffer fromBuffersDirect(ByteBuffer self, ByteBuffer... buffers) {

		return buffers
			.inject(ByteBuffer.allocateDirectNative(buffers.sum { it.limit() })) { acc, b -> acc.put(b) }
			.rewind()
	}

	/**
	 * Wraps a {@code byte[]} in a {@code ByteBuffer} with native byte ordering.
	 * 
	 * @param self
	 * @param array
	 * @return Natively ordered {@code ByteBuffer}.
	 */
	static ByteBuffer wrapNative(ByteBuffer self, byte[] array) {

		return ByteBuffer
			.wrap(array)
			.order(ByteOrder.nativeOrder())
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

		return ByteBuffer
			.wrap(array, offset, length)
			.order(ByteOrder.nativeOrder())
	}
}
