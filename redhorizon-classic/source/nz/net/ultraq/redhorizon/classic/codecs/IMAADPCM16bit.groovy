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
 * Decoder for the 16-bit IMA-ADPCM encoding scheme.  This decompression
 * technique is used with Red Alert's and Tiberium Dawn's 16-bit audio files.
 * <p>
 * A decoder instance will retain some state after each call to {@link #decode},
 * which is useful for decoding the next chunk in the same sequence.  A new
 * decoder should be created for each sound sequence being worked with.
 * <p>
 * Credit goes to Vladan Bato for the original decompression code, which I have
 * adapted below.  See: <a href="http://vladan.bato.net/cnc/aud3.txt" target="_top">http://vladan.bato.net/cnc/aud3.txt</a>
 * 
 * @author Emanuel Rabina
 */
@CompileStatic
class IMAADPCM16bit implements Decoder {

	// IMA-ADPCM adjustment table
	private static final int[] IMA_ADJUST_TABLE = [
		-1, -1, -1, -1, 2, 4, 6, 8
	]

	// IMA-ADPCM step table
	private static final int[] IMA_STEP_TABLE = [
		    7,     8,     9,    10,    11,    12,     13,    14,    16,
		   17,    19,    21,    23,    25,    28,     31,    34,    37,
		   41,    45,    50,    55,    60,    66,     73,    80,    88,
		   97,   107,   118,   130,   143,   157,    173,   190,   209,
		  230,   253,   279,   307,   337,   371,    408,   449,   494,
		  544,   598,   658,   724,   796,   876,    963,  1060,  1166,
		 1282,  1411,  1552,  1707,  1878,  2066,   2272,  2499,  2749,
		 3024,  3327,  3660,  4026,  4428,  4871,   5358,  5894,  6484,
		 7132,  7845,  8630,  9493, 10442, 11487,  12635, 13899, 15289,
		16818, 18500, 20350, 22385, 24623, 27086,  29794, 32767
	]

	private int lastIndex = 0
	private int lastSample = 0

	/**
	 * Decode a sound sample in IMA-ADPCM format.
	 */
	@Override
	ByteBuffer decode(ByteBuffer source, ByteBuffer dest) {

		// Until all the compressed data has been decompressed
		for (int sampleIndex = 0; sampleIndex < source.limit() << 1; sampleIndex++) {

			// The 4-bit command
			def code = source.get(sampleIndex >> 1)
			code = sampleIndex % 2 == 1 ? code >>> 4 : code & 0x0f

			def step = IMA_STEP_TABLE[lastIndex]
			def delta = step >>> 3

			// Expansion of the multiplication in the original pseudo code
			if (code & 0x01) {
				delta += step >>> 2
			}
			if (code & 0x02) {
				delta += step >>> 1
			}
			if (code & 0x04) {
				delta += step
			}

			// Sign bit = 1
			if (code & 0x08) {
				lastSample -= delta
				lastSample = Math.max(lastSample, Short.MIN_VALUE)
			}
			// Sign bit = 0
			else {
				lastSample += delta
				lastSample = Math.min(lastSample, Short.MAX_VALUE)
			}

			// Save result to destination buffer
			dest.putShort((short)lastSample)

			// Index/Step adjustments
			lastIndex += IMA_ADJUST_TABLE[code & 0x07]
			lastIndex = Math.clamp(lastIndex, 0, 88)
		}
		return dest.flip()
	}
}
