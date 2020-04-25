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

		return width < limitX ? Math.min(limitX / width as int, self.length) : 1
	}

	/**
	 * Return a new image buffer where the width and height dimensions have been
	 * right-shifted by the given amount.  (The reason for only scaling by binary
	 * shifting is that the scaling algorithm is really slow if it has to use
	 * division!)
	 * 
	 * @param self
	 * @param width
	 * @param height
	 * @param format
	 * @param scaleShift
	 * @return
	 */
	static ByteBuffer scale(ByteBuffer self, int width, int height, int format, int scaleShift) {

		def scaledWidth = width << scaleShift
		def scaledHeight = height << scaleShift
		def scaledBuffer = ByteBuffer.allocateNative(scaledWidth * scaledHeight * format)

		for (def y = 0; y < scaledHeight; y++) {
			for (def x = 0; x < scaledWidth; x++) {
				def selfPointer = ((y >> scaleShift) * width + (x >> scaleShift)) * format
				def scalePointer = (y * scaledWidth + x) * format
				scaledBuffer.position(scalePointer).put(self.array(), selfPointer, format)
			}
		}
		return scaledBuffer.rewind()
	}
}
