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
import groovy.transform.TupleConstructor
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Decoder for the {@code [*Pack]} sections found inside C&C map file data.
 *
 * @author Emanuel Rabina
 */
@CompileStatic
@TupleConstructor(defaults = false)
class PackData implements Decoder {

	private final Base64.Decoder base64Decoder = Base64.getDecoder()
	private final LCW lcw = new LCW()

	final int chunks

	@Override
	ByteBuffer decode(ByteBuffer source, ByteBuffer dest) {

		// Decode base64 data into standard binary
		ByteBuffer mapBytes2 = base64Decoder.decode(source).order(ByteOrder.nativeOrder())

		// Decode pack data, 'chunks' number of chunks
		ByteBuffer[] mapChunks = new ByteBuffer[chunks]
		for (int i = 0; i < chunks; i++) {
			mapChunks[i] = ByteBuffer.allocateNative(8192)
		}

		int pos = 0
		for (int i = 0; i < chunks; i++) {

			// Get following chunk size, skip 0x20 (unknown) byte
			int a = mapBytes2.get() & 0xff
			int b = mapBytes2.get() & 0xff
			int c = mapBytes2.get() & 0xff
			int d = mapBytes2.get() & 0xff // Is always 0x20, but unused
			assert d == 0x20
			int chunksize = (c << 16) | (b << 8) | a

			// Decode that chunk, put it into one of the buffers in the array
			lcw.decode(mapBytes2, mapChunks[i])
			pos += 4 + chunksize
			mapBytes2.position(pos)
		}

		// Collate chunks into total map data
		for (ByteBuffer mapchunk: mapChunks) {
			dest.put(mapchunk)
		}
		return dest.flip()
	}
}
