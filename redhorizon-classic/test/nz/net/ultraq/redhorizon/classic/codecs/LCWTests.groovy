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

package nz.net.ultraq.redhorizon.classic.codecs

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

	def "1 - 0cccpppp p = Copy c + 3 bytes from dest.pos - p to dest.pos"() {
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

	def "1 - copy range can overlap current position"() {
		given:
			def command = 0
			def copy = 5 // + 3 = 8
			def pos = 2
			def source = ByteBuffer.wrapNative([
				command | ((copy << 4) & 0x70) | ((pos >>> 8) & 0xf),
				pos,
				0x80
			] as byte[])
			def dest = ByteBuffer.wrapNative([
				1, 1, 0, 0, 0, 0, 0, 0, 0, 0
			] as byte[]).position(2)
		when:
			lcw.decode(source, dest)
		then:
			dest.array() == [
				1, 1, 1, 1, 1, 1, 1, 1, 1, 1
			] as byte[]
	}

	def "2 - 10cccccc = Copy next c bytes from source to dest"() {
		given:
			def command = 0x80
			def copy = 5
			def source = ByteBuffer.wrapNative([
			  command | (copy & 0x3f),
				1, 1, 1, 1, 1,
				0x80
			] as byte[])
			def dest = ByteBuffer.wrapNative([
			  0, 0, 0, 0, 0
			] as byte[])
		when:
			lcw.decode(source, dest)
		then:
			dest.array() == [
			  1, 1, 1, 1, 1
			] as byte[]
	}

	def "3 - 11cccccc p p = Copy c + 3 bytes from p"() {
		given:
			def command = 0xc0
			def copy = 2 // + 3 = 5
			def pos = 0
			def source = ByteBuffer.wrapNative([
			  command | (copy & 0x3),
				pos & 0xff,
				pos >>> 8 & 0xff,
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

	def "4 - 11111110 c c v = Write c bytes with v"() {
		given:
			def command = 0xfe
			def write = 10
			def val = 1
			def source = ByteBuffer.wrapNative([
			  command,
				write & 0xff,
				write >>> 8 & 0xff,
				val,
				0x80
			] as byte[])
			def dest = ByteBuffer.allocateNative(10)
		when:
			lcw.decode(source, dest)
		then:
			dest.array() == [
				1, 1, 1, 1, 1, 1, 1, 1, 1, 1
			] as byte[]
	}

	def "5 - 11111111 c c p p = Copy c bytes from p"() {
		given:
			def command = 0xff
			def copy = 5
			def pos = 3
			def source = ByteBuffer.wrapNative([
			  command,
				copy & 0xff,
				copy >>> 8 & 0xff,
				pos & 0xff,
				pos >>> 8 & 0xff,
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
