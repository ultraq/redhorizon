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
 * Decoder of the "XOR Delta" compression scheme.  For details about XOR Delta,
 * see: <a href="http://www.shikadi.net/moddingwiki/Westwood_XOR_Delta">http://www.shikadi.net/moddingwiki/Westwood_XOR_Delta</a>
 * Although credit goes to Vladan Bato for the description of what was
 * previously known as the "Format40" from which the code below is adapted.
 * See: <a href="http://vladan.bato.net/cnc/ccfiles4.txt">http://vladan.bato.net/cnc/ccfiles4.txt</a>
 *
 * <p>A decoder instance will retain some state after each call to {@link #decode},
 * which is useful for decoding the next image in the same sequence.  A new
 * decoder should be created for each image sequence being worked with.
 *
 * <p>Using a notation found in XCCU, the XOR Delta commands are as follows:
 * <ol>
 *   <li>00000000 c v = XOR next c bytes with v.</li>
 *   <li>0ccccccc = XOR the next c bytes from source with those in base.</li>
 *   <li>10000000 0c c = SKIP the next c bytes.</li>
 *   <li>10000000 10c c = XOR the next c bytes from source with those in base.</li>
 *   <li>10000000 11c c v = XOR the next c bytes with v.</li>
 *   <li>1ccccccc = Skip the next c bytes.</li>
 * </ol>
 *
 * @author Emanuel Rabina
 */
@CompileStatic
class XORDelta implements Decoder {

	private ByteBuffer xorSource

	/**
	 * Create a new decoder using an XOR source of the given size.
	 */
	XORDelta(int frameSize) {

		xorSource = ByteBuffer.allocateNative(frameSize)
	}

	@Override
	ByteBuffer decode(ByteBuffer source, ByteBuffer dest) {

		while (true) {
			byte command = source.get()
			int count

			// b7 = 0
			if (!(command & 0x80)) {

				// Command #1 - small XOR base with value
				if (!command) {
					count = source.get() & 0xff
					byte fill = source.get()
					while (count--) {
						dest.put((byte)(xorSource.get() ^ fill))
					}
				}
				// Command #2 - small XOR source with base for count
				else {
					count = command
					while (count--) {
						dest.put((byte)(source.get() ^ xorSource.get()))
					}
				}
			}
			// b7 = 1
			else {
				count = command & 0x7f

				// b6-0 = 0
				if (!count) {
					count = source.getShort() & 0xffff
					command = (byte)(count >>> 8)

					// b7 of next byte = 0
					if (!(command & 0x80)) {

						// Finished decoding
						if (!count) {
							break
						}

						// Command #3 - large copy base to dest for count
						dest.put(xorSource, count)
					}
					// b7 of next byte = 1
					else {
						count &= 0x3fff

						// Command #4 - large XOR source with base for count
						if (!(command & 0x40)) {
							while (count--) {
								dest.put((byte)(source.get() ^ xorSource.get()))
							}
						}
						// Command #5 - large XOR base with value
						else {
							byte fill = source.get()
							while (count--) {
								dest.put((byte)(xorSource.get() ^ fill))
							}
						}
					}
				}
				// b6-0 != 0
				else {

					// Command #6 - small copy base to dest for count
					dest.put(xorSource, count)
				}
			}
		}
		xorSource.rewind()
		xorSource = dest
		source.rewind()
		return dest.flip()
	}

	/**
	 * Change the base frame being used for the next {@link #decode} operation.
	 */
	XORDelta withDeltaSource(ByteBuffer deltaSource) {

		xorSource = deltaSource
		return this
	}
}
