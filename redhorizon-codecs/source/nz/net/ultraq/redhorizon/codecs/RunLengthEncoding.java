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

package nz.net.ultraq.redhorizon.codecs;

import java.nio.ByteBuffer;

/**
 * A basic, configurable, run-length decoder.
 *
 * @author Emanuel Rabina
 */
public class RunLengthEncoding implements Decoder {

	private final byte countByte;

	/**
	 * Constructor, configures the run-length decoding to recognize the given
	 * byte as the 'count' byte.
	 * 
	 * @param countByte
	 */
	public RunLengthEncoding(byte countByte) {

		this.countByte = countByte;
	}

	@Override
	public void decode(ByteBuffer source, ByteBuffer dest, ByteBuffer... extra) {

		while (source.hasRemaining() && dest.hasRemaining()) {
			byte value = source.get();

			// Count byte & copy byte run
			if ((value & countByte) == countByte) {
				int count = value & ~countByte;
				byte copy = source.get();

				while (count-- > 0) {
					dest.put(copy);
				}
			}
			// Non-count byte
			else {
				dest.put(value);
			}
		}
		dest.rewind();
	}
}
