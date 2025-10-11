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

package nz.net.ultraq.redhorizon.graphics.extensions

import nz.net.ultraq.redhorizon.graphics.Palette

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

		var dest = ByteBuffer.allocateNative(self.limit() * palette.channels)
		while (self.hasRemaining()) {
			dest.put(palette[self.get() & 0xff])
		}
		self.rewind()
		return dest.flip()
	}

	/**
	 * If an image buffer contains an image less than the specified dimensions,
	 * return an image buffer padded with 0s such that it will appear in the
	 * center.
	 */
	static ByteBuffer center(ByteBuffer self, int imageWidth, int imageHeight, int targetWidth, int targetHeight) {

		if (imageWidth < targetWidth || imageHeight < targetHeight) {
			var newTileImageData = ByteBuffer.allocateNative(targetWidth * targetHeight)
			var xOffset = Math.floor((targetWidth - imageWidth) / 2 as int) as int
			var yOffset = Math.floor((targetHeight - imageHeight) / 2 as int) as int
			imageHeight.times { y ->
				imageWidth.times { x ->
					newTileImageData.put(((yOffset + y) * targetWidth) + (xOffset + x), self.get((y * imageWidth) + x))
				}
			}
			return newTileImageData
		}
		return self
	}

	/**
	 * Creates a single overall image buffer from a series of smaller image
	 * buffers.
	 *
	 * @param self
	 * @param width Width of each image.
	 * @param height Height of each image
	 * @param channels
	 * @param imagesX Number of images to fit on the X axis.
	 * @return Single combined image buffer.
	 */
	static ByteBuffer combine(ByteBuffer[] self, int width, int height, int channels, int imagesX) {

		var imagesY = Math.ceil(self.length / imagesX as double) as int
		var compileWidth = width * channels * imagesX as int
		var compileHeight = height * imagesY
		var compilation = ByteBuffer.allocateNative(compileWidth * compileHeight)

		// For each image
		self.eachWithIndex { image, i ->
			var compilationPointer = (i / imagesX as int) * (compileWidth * height) + ((i % imagesX) * width)

			// For each vertical line of pixels in the current image
			height.times { y ->
				compilation
					.position(compilationPointer)
					.put(image, width * channels)
				compilationPointer += compileWidth
			}
			image.rewind()
		}
		return compilation.rewind()
	}

	/**
	 * Return a new image buffer where the pixel data on the vertical axis has
	 * been flipped.  This is used to make the Y axis of an image format, where
	 * often 0 means the top row of pixels, match a coordinate system where 0
	 * means the bottom row of pixels.
	 *
	 * @param self
	 * @param width Width of the image.
	 * @param height Height of the image.
	 * @param channels The number of colour channels in each pixel.
	 * @return A new buffer with the horizontal pixel data flipped.
	 */
	static ByteBuffer flipVertical(ByteBuffer self, int width, int height, int channels) {

		var flippedImageBuffer = ByteBuffer.allocateNative(self.capacity())
		var rowSize = width * channels
		height.times { y ->
			flippedImageBuffer.put(self.array(), rowSize * (height - 1 - y), rowSize)
		}
		return flippedImageBuffer.flip()
	}

	/**
	 * Flip a series of image buffers.  Calls {@link #flipVertical} for each
	 * buffer in the array.
	 *
	 * @param self
	 * @param width Width of each image.
	 * @param height Height of each image.
	 * @param channels The number of colour channels in each pixel.
	 * @return A new array of buffers whose pixel data has been flipped.
	 */
	static ByteBuffer[] flipVertical(ByteBuffer[] self, int width, int height, int channels) {

		return self.collect { image -> flipVertical(image, width, height, channels) } as ByteBuffer[]
	}
}
