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

import nz.net.ultraq.redhorizon.extensions.ByteBufferOrdering

import org.junit.Test

import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Tests for the {@link ByteBufferOrdering} extension module.
 * 
 * @author Emanuel Rabina
 */
class ByteBufferOrderingTests {

	/**
	 * Test the {@code allocateDirectNative} method returns a natively ordered
	 * buffer.
	 */
	@Test
	void allocateDirectNative() {

		def buffer = ByteBuffer.allocateDirectNative(8);
		assert buffer.order() == ByteOrder.nativeOrder()
	}

	/**
	 * Test the {@code allocateNative} method returns a natively ordered buffer.
	 */
	@Test
	void allocateNative() {

		def buffer = ByteBuffer.allocateNative(8);
		assert buffer.order() == ByteOrder.nativeOrder()
	}

	/**
	 * Test the {@code wrap} method returns a natively ordered buffer.
	 */
	@Test
	void wrapNative() {

		def buffer = ByteBuffer.wrapNative([0x0f] as byte[]);
		assert buffer.order() == ByteOrder.nativeOrder()
	}

	/**
	 * Test the {@code wrap} method returns a natively ordered buffer.
	 */
	@Test
	void wrapNativeOffsetLength() {

		def buffer = ByteBuffer.wrapNative([0xca, 0xfe] as byte[], 0, 2);
		assert buffer.order() == ByteOrder.nativeOrder()
	}
}
