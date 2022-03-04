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

package nz.net.ultraq.redhorizon.filetypes.extensions

import nz.net.ultraq.redhorizon.filetypes.ColourFormat
import nz.net.ultraq.redhorizon.filetypes.Palette

import groovy.transform.CompileStatic
import java.nio.ByteBuffer

/**
 * Extensions to the {@link ByteBuffer} object for when holding image data.
 * 
 * @author Emanuel Rabina
 */
@CompileStatic
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
		return dest.flip()
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

		def imagesY = (self.length / imagesX) + 1 as int
		def compileWidth = width * imagesX
		def compileHeight = height * imagesY
		def compilation = ByteBuffer.allocateNative(compileWidth * compileHeight)

		// For each image
		self.eachWithIndex { image, i ->
			def compilationPointer = (i / imagesX as int) * (compileWidth * height) + ((i % imagesX) * width)

			// For each vertical line of pixels in the current image
			height.times { y ->
				compilation
					.position(compilationPointer)
					.put(image, width)
				compilationPointer += compileWidth
			}
			image.rewind()
		}
		return compilation.flip()
	}

	/**
	 * Return a new image buffer where the pixels data on the vertical axis has
	 * been flipped.  This is used to make the Y axis of an image format, where
	 * often 0 means the top row of pixels, match a coordinate system where 0
	 * means the bottom row of pixels.
	 * 
	 * @param self
	 * @param width  Width of the image.
	 * @param height Height of the image.
	 * @param format The number of colour channels in each pixel.
	 * @return A new buffer with the horizontal pixel data flipped.
	 */
	static ByteBuffer flipVertical(ByteBuffer self, int width, int height, ColourFormat format) {

		def flippedImageBuffer = ByteBuffer.allocateNative(self.capacity())
		def rowSize = width * format.value
		height.times { int y ->
			flippedImageBuffer.put(self.array(), rowSize * (height - 1 - y), rowSize)
		}
		return flippedImageBuffer.flip()
	}
}
