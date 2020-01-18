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

package nz.net.ultraq.redhorizon.filetypes.mix

import spock.lang.Specification
import spock.lang.Unroll

import java.nio.ByteBuffer

/**
 * An attempt to capture inputs/outputs of the MixFileKey code to wrap in unit
 * tests for being able to translate the C++ to Groovy one day.
 * 
 * @author Emanuel Rabina
 */
class MixFileKeySpecification extends Specification {

	@Unroll
	def "Key decryption for #mixFile"(String mixFile, ByteBuffer source, ByteBuffer key) {
		expect:
			def dest = ByteBuffer.allocateNative(56)
			MixFileKey.getBlowfishKey(source, dest)
			dest.array() == key.array()

		where:
			mixFile << [
			  "Conquer.mix",
				"Voices_Allies.mix"
			]
			source << [
				ByteBuffer.wrapNative([0x2a, 0x21, 0x5d, 0x1a, 0x6c, 0x80, 0x0d, 0xea, 0x70, 0x0b, 0xe7, 0x9f, 0xee, 0xc7, 0x50, 0x69, 0x87, 0x3a, 0xf1, 0x14, 0x6e, 0x55, 0xac, 0x94, 0x2c, 0xd4, 0xd1, 0xc0, 0x52, 0xf7, 0x13, 0x5d, 0x28, 0x6b, 0x54, 0xd6, 0x64, 0x91, 0xa5, 0x1f, 0x46, 0x5b, 0x1b, 0x1c, 0x80, 0x5a, 0x70, 0x5f, 0xa2, 0x48, 0xa2, 0x58, 0x7c, 0xb3, 0xbd, 0xe5, 0x15, 0x3c, 0x54, 0x57, 0xb6, 0xba, 0x3f, 0xb5, 0x05, 0xbe, 0x2f, 0x91, 0x57, 0x80, 0xf9, 0x69, 0x5e, 0xcb, 0xbe, 0xf3, 0xf0, 0xd5, 0x9c, 0x4b] as byte[]),
				ByteBuffer.wrapNative([0x07, 0x88, 0x31, 0x01, 0x99, 0x18, 0x8b, 0xb4, 0xdf, 0x89, 0x47, 0x2b, 0xf9, 0x5a, 0xd0, 0xff, 0x15, 0x22, 0xfc, 0xd7, 0x12, 0x5c, 0x13, 0xc4, 0x02, 0xa5, 0x7a, 0x0f, 0x85, 0xea, 0x11, 0xb2, 0xcb, 0xc9, 0x95, 0x33, 0x4f, 0xce, 0x88, 0x16, 0x18, 0x46, 0x07, 0x4b, 0xff, 0x25, 0x7e, 0xe1, 0x86, 0x44, 0x22, 0xee, 0xa5, 0x9c, 0x90, 0x1d, 0x05, 0x0b, 0x91, 0xa9, 0x70, 0xbc, 0xe3, 0xe2, 0xcb, 0x3b, 0x56, 0x10, 0xbd, 0xd5, 0x13, 0x27, 0x3c, 0xa6, 0x85, 0xde, 0xc2, 0xa0, 0x00, 0x3b] as byte[])
			]
			key << [
			  ByteBuffer.wrapNative([0x0a, 0xde, 0x83, 0xe3, 0xc6, 0x30, 0xd1, 0xbb, 0x4c, 0x25, 0xcb, 0x61, 0xe4, 0xb2, 0x5a, 0x91, 0x39, 0x42, 0x56, 0xe3, 0x2b, 0xe0, 0x0f, 0xc0, 0x7e, 0x90, 0xcb, 0x21, 0xdb, 0x4b, 0xa8, 0x8b, 0x23, 0xed, 0x22, 0xab, 0x63, 0xcc, 0x95, 0x22, 0xdb, 0x50, 0x60, 0x10, 0x50, 0xa1, 0x23, 0x23, 0x44, 0x49, 0xd4, 0x2f, 0x84, 0x8f, 0x35, 0xbe] as byte[]),
				ByteBuffer.wrapNative([0x8a, 0xe1, 0xd0, 0xfd, 0x25, 0xb9, 0x67, 0xa7, 0x94, 0x99, 0xc1, 0x22, 0x87, 0x95, 0x6c, 0x24, 0x22, 0x51, 0xa5, 0x9c, 0xaf, 0x89, 0x06, 0x03, 0xe3, 0xef, 0xe0, 0xa8, 0x0c, 0x67, 0x91, 0x09, 0x67, 0x76, 0xbe, 0xaf, 0xbb, 0x85, 0x5f, 0x9f, 0xf3, 0xa0, 0xa5, 0x61, 0x10, 0x92, 0x28, 0x57, 0x2c, 0x99, 0x4b, 0xd8, 0x1a, 0xcf, 0x17, 0x5c] as byte[])
			]
	}
}
