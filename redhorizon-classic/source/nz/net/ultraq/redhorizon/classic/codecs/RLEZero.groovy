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
 * Decoder of Westwood's "RLE-Zero" compression scheme.  For details about
 * RLE-Zero, see: <a href="http://www.shikadi.net/moddingwiki/Westwood_RLE-Zero">http://www.shikadi.net/moddingwiki/Westwood_RLE-Zero</a>
 *
 * <p>A RLE-Zero file can be decoded as follows:
 * <ol>
 *   <li>0 c = Fill the next c bytes with 0</li>
 *   <li>v   = Write v</li>
 * </ol>
 *
 * @author Emanuel Rabina
 */
@CompileStatic
class RLEZero implements Decoder {

	// @formatter:off
	private static final byte CMD_FILL     = 0
	private static final byte CMD_FILL_VAL = 0
	// @formatter:on

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
}
