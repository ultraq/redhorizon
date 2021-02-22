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

import nz.net.ultraq.redhorizon.codecs.Decoder
import nz.net.ultraq.redhorizon.codecs.Encoder

import groovy.transform.CompileStatic
import java.nio.ByteBuffer

/**
 * Encoder/decoder utilizing Westwood's "RLE-Zero" compression scheme.
 * <p>
 * For details about RLE-Zero, see: http://www.shikadi.net/moddingwiki/Westwood_RLE-Zero
 * <p>
 * A RLE-Zero file can be decoded as follows:
 * <ol>
 *   <li>0 c = Fill the next c bytes with 0<li>
 *   <li>v   = Write v<li>
 * </ol>
 * 
 * @author Emanuel Rabina
 */
@CompileStatic
class RLEZero implements Encoder, Decoder {

	private static final byte CMD_FILL     = 0
	private static final byte CMD_FILL_VAL = 0

	@Override
	ByteBuffer decode(ByteBuffer source, ByteBuffer dest) {

		while (source.hasRemaining()) {
			byte command = source.get()

			// Fill 0s
			if (command == CMD_FILL) {
				int count = source.get() & 0xff
				while (count--) {
					dest.put(CMD_FILL_VAL)
				}
			}
			// Write direct value
			else {
				dest.put(command)
			}
		}
		return dest.flip()
	}

	@Override
	ByteBuffer encode(ByteBuffer source, ByteBuffer dest) {

		int count = 0
		int limit = Math.min(source.limit(), 255)

		outer: while (source.hasRemaining()) {
			byte value = source.get()

			// Count a series of 0s, describe the series
			while (value == CMD_FILL_VAL) {
				while (value == CMD_FILL_VAL && count < limit) {
					count++
					if (source.hasRemaining()) {
						value = source.get()
					}
					else {
						break
					}
				}
				dest.put([ CMD_FILL, (byte)count ] as byte[])
				count = 0
				if (!source.hasRemaining()) {
					break outer
				}
			}

			// Write non-0 value
			dest.put(value)
		}
		source.rewind()
		return dest.flip()
	}
}
