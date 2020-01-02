/* 
 * Copyright 2019, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.extensions

import nz.net.ultraq.redhorizon.filetypes.Palette

import java.nio.ByteBuffer

/**
 * Extensions to the {@link ByteBuffer} object for when holding image data.
 * 
 * @author Emanuel Rabina
 */
class ByteBufferImageExtensions {

	/**
	 * Applies a palette to indexed image data, returning a buffer of full colour
	 * image data.
	 * 
	 * @param self
	 * @param palette Palette data to use.
	 * @return A new {@code ByteBuffer} of the combined indexed and palette data.
	 */
	static ByteBuffer applyPalette(ByteBuffer self, Palette palette) {

		ByteBuffer dest = ByteBuffer.allocateNative(self.limit() * palette.format.value)
		while (self.hasRemaining()) {
			dest.put(palette[self.get() & 0xff])
		}
		self.rewind()
		return dest.rewind()
	}
}
