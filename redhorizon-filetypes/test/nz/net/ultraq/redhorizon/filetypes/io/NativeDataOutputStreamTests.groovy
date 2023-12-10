/*
 * Copyright 2023, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.filetypes.io

import spock.lang.Specification

/**
 * Tests for the primitives stream that aims to respect the platform byte order.
 *
 * @author Emanuel Rabina
 */
class NativeDataOutputStreamTests extends Specification {

	def "writeInt"() {
		given:
			var bos = new ByteArrayOutputStream()
			var outputStream = new NativeDataOutputStream(bos)
		when:
			outputStream.writeInt(33177600) // 7680x4320, ie: 8k 😆
			outputStream.flush()
		then:
			var bytes = bos.toByteArray()
			(bytes[0] & 0x00ff) == 0x00
			(bytes[1] & 0x00ff) == 0x40
			(bytes[2] & 0x00ff) == 0xfa
			(bytes[3] & 0x00ff) == 0x01
	}

	def "writeShort"() {
		given:
			var bos = new ByteArrayOutputStream()
			var outputStream = new NativeDataOutputStream(bos)
		when:
			outputStream.writeShort(64000) // 320x200
			outputStream.flush()
		then:
			var bytes = bos.toByteArray()
			(bytes[0] & 0x00ff) == 0x00
			(bytes[1] & 0x00ff) == 0xfa
	}
}
