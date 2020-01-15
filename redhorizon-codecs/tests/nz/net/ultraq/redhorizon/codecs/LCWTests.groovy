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

package nz.net.ultraq.redhorizon.codecs

import spock.lang.Specification

import java.nio.ByteBuffer

/**
 * Tests for the LCW codec.
 * 
 * @author Emanuel Rabina
 */
class LCWTests extends Specification {

	def lcw = new LCW()

	def "Delimiter"() {
		given:
			def source = ByteBuffer.wrapNative([
			  0x80
			] as byte[])
			def dest = ByteBuffer.allocateNative(1)
		when:
			lcw.decode(source, dest)
		then:
			dest.array() == [0] as byte[]
			dest.limit() == 0
			dest.position() == 0
	}

	def "First command"() {
		given:
			def command = 0
			def copy = 2 // + 3 = 5
			def pos = 5
			def source = ByteBuffer.wrapNative([
				command | ((copy << 4) & 0x70) | ((pos >>> 8) & 0xf),
				pos,
				0x80
			] as byte[])
			def dest = ByteBuffer.wrapNative([
			  1, 1, 1, 1, 1, 0, 0, 0, 0, 0
			] as byte[]).position(5)
		when:
			lcw.decode(source, dest)
		then:
			dest.array() == [
			  1, 1, 1, 1, 1, 1, 1, 1, 1, 1
			] as byte[]
	}
}
