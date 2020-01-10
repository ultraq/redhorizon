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

		def dest = ByteBuffer.allocateNative(self.limit() * palette.format.value)
		while (self.hasRemaining()) {
			dest.put(palette[self.get() & 0xff])
		}
		self.rewind()
		return dest.rewind()
	}

	/**
	 * Creates a single overall image buffer from a series of smaller image
	 * buffers.
	 * 
	 * @param self
	 * @param width   Width of each image.
	 * @param height  Height of each image
	 * @param imagesX Number of images to fit on the X axis.
	 * @return Single combined image buffer.
	 */
	static ByteBuffer combineImages(ByteBuffer[] self, int width, int height, int imagesX) {

		def imagesY = Math.ceil(self.length / imagesX) as int
		def compileWidth = width * imagesX
		def compileHeight = height * imagesY
		def compilation = ByteBuffer.allocateNative(compileWidth * compileHeight)

		// For each image
		for (def image = 0; image < self.length; image++) {
			def compilationPointer = (image / imagesX as int) * (compileWidth * height) + ((image % imagesX) * width)

			// For each vertical line of pixels in the current image
			for (def y = 0; y < height; y++) {
				compilation
					.position(compilationPointer)
					.put(self[image], width)
				compilationPointer += compileWidth
			}
		}
		return compilation.rewind()
	}

	/**
	 * Given several image buffers, find out how many images will fit across so
	 * that the resulting combined width is less-than or equal-to {@code limitX}
	 * pixels.
	 * 
	 * @param self
	 * @param width  Width of each image.
	 * @param limitX Desired combined image width to stay below.
	 * @return Number of images that will fit within {@code limitX} pixels.
	 */
	static int imagesAcross(ByteBuffer[] self, int width, int limitX) {

		return width < limitX ? Math.min(Math.floor(limitX / width), self.length) : 1
	}
}
