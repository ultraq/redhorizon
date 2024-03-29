/* 
 * Copyright 2007, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import nz.net.ultraq.redhorizon.filetypes.codecs.Decoder

import groovy.transform.CompileStatic
import java.nio.ByteBuffer

/**
 * Decoder for the 8-bit Westwood Studios ADPCM encoding scheme.  This is used
 * for very few sound samples in both Red Alert and Tiberium Dawn, most notably
 * the infantry death sounds.
 * <p>
 * Credit goes to Asatur V. Nazarian for the original decompression code, which
 * I have adapted below.  (No link to their original documents unfortunately 😢)
 * 
 * @author Emanuel Rabina
 */
@CompileStatic
class WSADPCM8bit implements Decoder {

	// WS-ADPCM 2-bit adjustment table
	private static final int[] WS_TABLE_2BIT = [
		-2, -1, 0, 1
	]

	// WS-ADPCM 4-bit adjustment table
	private static final int[] WS_TABLE_4BIT = [
		-9, -8, -6, -5, -4, -3, -2, -1, 0,  1,  2,  3,  4,  5,  6,  8
	]

	@Override
	ByteBuffer decode(ByteBuffer source, ByteBuffer dest) {

		// Mere copy if chunk is uncompressed
		if (source.limit() == dest.limit()) {
			return dest.put(source).flip()
		}

		// Decompression
		int sample = 0x80
		while (dest.hasRemaining()) {

			short input = source.getShort()
			input <<= 2
			byte command = (byte)(input >>> 8)
			byte count   = (byte)((input & 0xff) >> 2)

			// No compression
			if (command == 2) {
				if (count & 0x20) {
					count <<= 3
					sample += count >> 3
					dest.put((byte)sample)
				}
				else {
					dest.put(source, count)
					sample = dest.get(dest.position() - 1)
					sample &= 0xffff
				}
			}

			// 2x compression (4-bit -> 8-bit)
			else if (command == 1) {
				while (count--) {
					command = source.get()

					sample += WS_TABLE_4BIT[command & 0x0f]
					sample = Math.clamp(sample, 0, 255)
					dest.put((byte)sample)

					sample += WS_TABLE_4BIT[command >>> 4]
					sample = Math.clamp(sample, 0, 255)
					dest.put((byte)sample)
				}
			}

			// 4x compression (2-bit -> 8-bit)
			else if (command == 0) {
				while (count--) {
					command = source.get()

					sample += WS_TABLE_2BIT[command & 0x03]
					sample = Math.clamp(sample, 0, 255)
					dest.put((byte)sample)

					sample += WS_TABLE_2BIT[(command >>> 2) & 0x03]
					sample = Math.clamp(sample, 0, 255)
					dest.put((byte)sample)

					sample += WS_TABLE_2BIT[(command >>> 4) & 0x03]
					sample = Math.clamp(sample, 0, 255)
					dest.put((byte)sample)

					sample += WS_TABLE_2BIT[(command >>> 6) & 0x03]
					sample = Math.clamp(sample, 0, 255)
					dest.put((byte)sample)
				}
			}

			// Straight copy
			else {
				while (count--) {
					dest.put((byte)sample)
				}
			}
		}
		return dest.flip()
	}
}
