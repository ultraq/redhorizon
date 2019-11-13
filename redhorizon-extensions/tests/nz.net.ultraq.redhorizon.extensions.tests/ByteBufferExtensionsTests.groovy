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

package nz.net.ultraq.redhorizon.extensions.tests

import nz.net.ultraq.redhorizon.extensions.ByteBufferExtensions

import spock.lang.Specification

import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Tests for the {@link ByteBufferExtensions} extension module.
 * 
 * @author Emanuel Rabina
 */
class ByteBufferExtensionsTests extends Specification {

	def "allocateDirectNative returns a natively ordered buffer"() {
		expect:
			ByteBuffer.allocateDirectNative(8).order() == ByteOrder.nativeOrder()
	}

	def "allocateNative returns a natively ordered buffer"() {
		expect:
			ByteBuffer.allocateNative(8).order() == ByteOrder.nativeOrder()
	}

	def "wrapNative returns a natively ordered buffer"() {
		expect:
			ByteBuffer.wrapNative([0x0f] as byte[]).order() == ByteOrder.nativeOrder()
	}

	def "wrapNative with offset & length returns a natively ordered buffer"() {
		expect:
			ByteBuffer.wrapNative([0xca, 0xfe] as byte[], 0, 2).order() == ByteOrder.nativeOrder()
	}
}
