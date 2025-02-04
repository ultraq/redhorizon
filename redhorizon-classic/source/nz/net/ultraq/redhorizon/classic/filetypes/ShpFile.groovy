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

package nz.net.ultraq.redhorizon.classic.filetypes

import nz.net.ultraq.redhorizon.classic.codecs.LCW
import nz.net.ultraq.redhorizon.classic.codecs.XORDelta
import nz.net.ultraq.redhorizon.filetypes.ColourFormat
import nz.net.ultraq.redhorizon.filetypes.FileExtensions
import nz.net.ultraq.redhorizon.filetypes.ImagesFile
import nz.net.ultraq.redhorizon.filetypes.io.NativeDataInputStream
import static nz.net.ultraq.redhorizon.filetypes.ColourFormat.FORMAT_INDEXED

import java.nio.ByteBuffer

/**
 * Implementation of the SHP file format as used in the orignal Command &
 * Conquer and Red Alert.  SHP files (shapes?) are a series of images used for
 * the visuals of a game object, like a unit or structure.
 * <p>
 * For more information about the C&C SHP file, see:
 * <a href="http://vladan.bato.net/cnc/ccfiles4.txt" target="_top">http://vladan.bato.net/cnc/ccfiles4.txt</a>
 *
 * @author Emanuel Rabina
 */
@FileExtensions('shp')
@SuppressWarnings('GrFinalVariableAccess')
class ShpFile implements ImagesFile {

	// @formatter:off
	static final int HEADER_SIZE = 14
	static final int OFFSET_SIZE = 8
	static final int MAX_WIDTH   = 65535
	static final int MAX_HEIGHT  = 65535

	// Header flags
	static final byte FORMAT_LCW       = (byte)0x80
	static final byte FORMAT_XOR_BASE  = (byte)0x40
	static final byte FORMAT_XOR_CHAIN = (byte)0x20
	// @formatter:on

	// File header
	final int numImages // Stored in file as short
	final short x
	final short y
	final int width     // Stored in file as short
	final int height    // Stored in file as short
	final short delta
	final short flags
	final ShpImageInfo[] imageOffsets

	final ColourFormat format = FORMAT_INDEXED

	private final NativeDataInputStream input
	@Lazy
	volatile ByteBuffer[] imagesData = { decodeImagesData() }()

	/**
	 * Constructor, creates a new SHP file from the given file data.
	 */
	ShpFile(InputStream inputStream) {

		input = new NativeDataInputStream(inputStream)

		// File header
		numImages = input.readShort() & 0xffff
		assert numImages > 0

		x = input.readShort()
		y = input.readShort()

		width = input.readShort() & 0xffff
		assert width > 0

		height = input.readShort() & 0xffff
		assert height > 0

		delta = input.readShort()
		assert delta >= 0

		flags = input.readShort()

		// numImages() + 2 for the 0 offset and EOF pointer
		imageOffsets = new ShpImageInfo[numImages + 2]
		imageOffsets.length.times { i ->
			var imageInfo = new ShpImageInfo(input)
			assert imageInfo.offsetFormat == 0 || imageInfo.offsetFormat in [FORMAT_LCW, FORMAT_XOR_BASE, FORMAT_XOR_CHAIN]
			imageOffsets[i] = imageInfo
		}
	}

	/**
	 * Decompress the SHP file data into the indexed image frames.
	 */
	private ByteBuffer[] decodeImagesData() {

		var lcw = new LCW()
		var xorDelta = new XORDelta(delta)

		var imagesData = new ByteBuffer[numImages]
		imagesData.length.times { i ->
			var imageOffset = imageOffsets[i]

			// Format conversion buffers
			var compressedImageSize = imageOffsets[i + 1].offset - imageOffset.offset
			var compressedImage = ByteBuffer.wrapNative(input.readNBytes(compressedImageSize))
			var uncompressedImage = ByteBuffer.allocateNative(width * height)

			imagesData[i] = switch (imageOffset.offsetFormat) {
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
		}

		return imagesData
	}

	/**
	 * Returns some information on this SHP file.
	 *
	 * @return SHP file info.
	 */
	@Override
	String toString() {

		return "SHP file (C&C), contains ${numImages} ${width}x${height} images (no palette)"
	}
}
