/*
 * Copyright 2025, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.classic.filetypes

import nz.net.ultraq.redhorizon.classic.codecs.LCW
import nz.net.ultraq.redhorizon.classic.codecs.XORDelta
import nz.net.ultraq.redhorizon.classic.io.NativeDataInputStream
import nz.net.ultraq.redhorizon.graphics.ImageDecoder

import java.nio.ByteBuffer

/**
 * An image decoder for the SHP file format as used in the orignal Command &
 * Conquer and Red Alert.  SHP files (shapes?) are a series of images used for
 * the visuals of a game object, like a unit or structure, and are equivalent to
 * a modern day sprite sheet.
 * <p>
 * For more information about the C&C SHP file, see:
 * <a href="http://vladan.bato.net/cnc/ccfiles4.txt" target="_top">http://vladan.bato.net/cnc/ccfiles4.txt</a>
 *
 * @author Emanuel Rabina
 */
class ShpFileDecoder implements ImageDecoder, FileTypeTest {

	// Header flags
	// @formatter:off
	static final byte FORMAT_LCW       = (byte)0x80
	static final byte FORMAT_XOR_BASE  = (byte)0x40
	static final byte FORMAT_XOR_CHAIN = (byte)0x20
	static final byte FORMAT_NONE      = (byte)0x00
	// @formatter:on

	String[] supportedFileExtensions = ['shp', 'int', 'sno', 'tem']

	@Override
	DecodeSummary decode(InputStream inputStream) {

		var input = new NativeDataInputStream(inputStream)

		// File header
		var numImages = input.readUnsignedShort()
		assert numImages > 0

		var x = input.readShort()
		var y = input.readShort()

		var width = input.readUnsignedShort()
		assert width > 0

		var height = input.readUnsignedShort()
		assert height > 0

		var delta = input.readShort()
		assert delta >= 0

		var flags = input.readShort()

		trigger(new HeaderDecodedEvent(width, height, 1, numImages, -1f))

		// numImages() + 2 for the 0 offset and EOF pointer
		var imageOffsets = new ShpImageInfo[numImages + 2]
		imageOffsets.length.times { i ->
			var imageInfo = new ShpImageInfo(input.readInt(), input.readInt())
			assert imageInfo.offsetFormat in [FORMAT_NONE, FORMAT_LCW, FORMAT_XOR_BASE, FORMAT_XOR_CHAIN]
			imageOffsets[i] = imageInfo
		}

		// Decode images and emit as frame events
		var lcw = new LCW()
		var xorDelta = new XORDelta(delta)
		var imagesData = new ByteBuffer[numImages]
		numImages.times { i ->
			var imageOffset = imageOffsets[i]

			// Format conversion buffers
			var compressedImageSize = imageOffsets[i + 1].offset - imageOffset.offset
			var compressedImage = ByteBuffer.wrapNative(input.readNBytes(compressedImageSize))
			var uncompressedImage = ByteBuffer.allocateNative(width * height)

			var imageData = switch (imageOffset.offsetFormat) {
				case FORMAT_LCW ->
					lcw.decode(compressedImage, uncompressedImage)
				case FORMAT_XOR_BASE ->
					xorDelta
						.deltaSource(imagesData[imageOffsets.findIndexOf { it.offset == imageOffset.refOff }])
						.decode(compressedImage, uncompressedImage)
				case FORMAT_XOR_CHAIN ->
					xorDelta
//						.deltaSource(imagesData[i - 1])
						.decode(compressedImage, uncompressedImage)
			}

			trigger(new FrameDecodedEvent(width, height, 1, imageData))
			imagesData[i] = imageData
		}

		return new DecodeSummary(width, height, 1, numImages,
			"SHP file (C&C), contains ${numImages} ${width}x${height} images (no palette)")
	}

	@Override
	void test(InputStream inputStream) {

		var input = new NativeDataInputStream(inputStream)

		var numImages = input.readUnsignedShort()
		assert numImages > 0

		input.skipBytes(4)

		var width = input.readUnsignedShort()
		assert width > 0

		var height = input.readUnsignedShort()
		assert height > 0

		var delta = input.readShort()
		assert delta >= 0

//		(numImages + 2).times { i ->
//			var imageInfo = new ShpImageInfo(input.readInt(), input.readInt())
//			assert imageInfo.offsetFormat in [FORMAT_NONE, FORMAT_LCW, FORMAT_XOR_BASE, FORMAT_XOR_CHAIN]
//		}
	}

	/**
	 * An image offset record found in SHP files.  Not all SHP files are just
	 * straight images, but some use a form of difference encoding which has to be
	 * matched to a certain key image to obtain the full frame.  The link to which
	 * frame has to be matched with which, is found in an offset.
	 */
	private class ShpImageInfo {

		final int offset
		final byte offsetFormat
		final int refOff
		final byte refOffFormat

		ShpImageInfo(int thisOffset, int referenceOffset) {

			offsetFormat = (byte)(thisOffset >>> 24)
			offset = thisOffset & 0x00ffffff

			refOffFormat = (byte)(referenceOffset >>> 24)
			refOff = referenceOffset & 0x00ffffff
		}
	}
}
