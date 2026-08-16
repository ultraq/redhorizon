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

import groovy.transform.CompileStatic
import java.nio.ByteBuffer

/**
 * Ddecoder of the "LCW" compression scheme.  For details about LCW, see:
 * <a href="http://www.shikadi.net/moddingwiki/Westwood_LCW">http://www.shikadi.net/moddingwiki/Westwood_LCW</a>
 * Although credit goes to Vladan Bato for the description of what was
 * previously known as the "Format80" from which the code below is adapted.
 * See: <a href="http://vladan.bato.net/cnc/ccfiles4.txt">http://vladan.bato.net/cnc/ccfiles4.txt</a>
 *
 * <p>Using a notation found in XCCU, the LCW commands are as follows:
 * <ol>
 *   <li>0cccpppp p = Copy c + 3 bytes from dest.pos - p to dest.pos</li>
 *   <li>10cccccc = Copy next c bytes from source to dest</li>
 *   <li>11cccccc p p = Copy c + 3 bytes from p</li>
 *   <li>11111110 c c v = Write c bytes with v</li>
 *   <li>11111111 c c p p = Copy c bytes from p</li>
 * </ol>
 *
 * @author Emanuel Rabina
 */
@CompileStatic
class LCW implements Decoder {

	@Override
	ByteBuffer decode(ByteBuffer source, ByteBuffer dest) {

		while (true) {
			byte command = source.get()
			int count, copyPos

			// b7 = 0
			if (!(command & 0x80)) {

				// Command #1 - copy bytes relative to the current position in dest.
				// This can overlap with the current position, so a bulk copy is not
				// easily doable.
				count = (command >>> 4) + 3
				copyPos = dest.position() - (((command & 0x0f) << 8) | (source.get() & 0xff))
				while (count--) {
					dest.put(dest.get(copyPos++))
				}
			}
			// b7 = 1
			else {
				count = command & 0x3f

				// b6 = 0
				if (!(command & 0x40)) {

					// Finished decoding
					if (!count) {
						break
					}

					// Command #2 - copy the next count bytes as is from source to dest.
					dest.put(source, count)
				}
				// b6 = 1
				else {

					// Command #3 - copy bytes from the given position in dest.
					if (count < 0x3e) {
						count += 3
						copyPos = source.getShort() & 0xffff
						while (count--) {
							dest.put(dest.get(copyPos++))
						}
					}
					// Command #4 - fill dest with the next byte for up to count bytes.
					else if (count == 0x3e) {
						count = source.getShort() & 0xffff
						byte fill = source.get()
						while (count--) {
							dest.put(fill)
						}
					}
					// Command #5 - copy bytes from the given position in dest.
					else {
						count = source.getShort() & 0xffff
						copyPos = source.getShort() & 0xffff
						while (count--) {
							dest.put(dest.get(copyPos++))
						}
					}
				}
			}
		}
		source.rewind()
		return dest.flip()
	}
}
