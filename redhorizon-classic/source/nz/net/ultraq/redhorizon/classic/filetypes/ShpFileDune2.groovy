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
import nz.net.ultraq.redhorizon.classic.codecs.RLEZero
import nz.net.ultraq.redhorizon.classic.io.NativeDataInputStream

import java.nio.ByteBuffer

/**
 * Implementation of the Dune 2 SHP file.  It shares many similarities with the
 * format in the Command and Conquer games, but each image within the file can
 * have its own dimensions.
 * <p>
 * The Dune 2 SHP file is only used for the conversion utility.
 * <p>
 * File header:
 * <ul>
 *   <li>NumImages (2 bytes) - the number of images in the file</li>
 *   <li>Offsets[NumImages + 1] (2 or 4 bytes each) - offset to the image header
 *     for an image.  The last offset points to the end of the file.  The
 *     offsets don't take into account the NumImages bytes at the beginning, so
 *     add 2 bytes to the offset value to get the actual position of an image
 *     header in the file</li>
 * </ul>
 * The size of the offsets can be either 2 or 4 bytes.  There is no simple way
 * to determine which it will be, but checking the file's 4th byte to see if
 * it's 0, seems to be a commonly accepted practice amongst existing Dune 2 SHP
 * file readers:
 * <p>
 * <code><pre>
 * A 2-byte offset file: 01 00 06 00 EC 00 45 0A ...
 * A 4-byte offset file: 01 00 08 00 00 00 EC 00 ...
 *                                   ^^
 * </pre></code>
 * <p>
 * The marked byte will be 0 in 4-byte offset files, non 0 in 2-byte offset
 * files.
 * <p>
 * Lastly, like C&C SHP files, there is an extra offset, pointing to the end of
 * the file (or what would have been the position of another image header/data
 * pair).
 * <p>
 * Following the file header, are a series of image header & image data pairs.
 * The image header is structured as follows:
 * <ul>
 *   <li>Flags (2 bytes) - flags to identify the type of data following the
 *     header, and/or the compression schemes used</li>
 *   <li>Slices (1 byte) - number of Format2 slices used to encode the image
 *     data.  Often this is the same as the height of the image</li>
 *   <li>Width (2 bytes) - width of the image</li>
 *   <li>Height (1 byte) - height of the image</li>
 *   <li>File size (2 bytes) - size of both this image header and the image data
 *     on the file</li>
 *   <li>Image size (2 bytes) - size of the image data in Format2 form.</li>
 * </ul>
 * Regarding the flags, there seem to be 3 known flags:
 * <ul>
 *   <li>The first bit controls whether there is a lookup table in the image
 *     data, used for remapping SHP colours to faction-specific colours in-game.
 *     0 = no lookup table, 1 = lookup table</li>
 *   <li>The second bit controls what compression to apply to the file.
 *     0 = RLE Zero, followed by LCW, 1 = RLE Zero only</li>
 *   <li>The third bit, used in conjunction with the first, means the first byte
 *     of the image data gives the size of the lookup table that follows.
 *     0 = Fixed-length lookup table (16 bytes), 1 = Variable-length lookup table</li>
 * </ul>
 * And after this image header is the image data.
 *
 * @author Emanuel Rabina
 */
@SuppressWarnings('GrFinalVariableAccess')
class ShpFileDune2 {

	static final int MAX_WIDTH = 65535
	static final int MAX_HEIGHT = 255

	// File header
	final int numImages
	final int[] imageOffsets

	// Image data
	final ByteBuffer[] imagesData

	/**
	 * Constructor, creates a new SHP file from the given file data.
	 *
	 * @param input
	 */
	ShpFileDune2(InputStream inputStream) {

		def input = new NativeDataInputStream(inputStream)

		// File header (read ahead for offset check)
		numImages = input.readShort()
		def offsetSize = input.markAndReset(4) {
			def readAhead = input.readNBytes(4)
			return readAhead[2] ? 2 : 4
		}

		// Image offset table
		imageOffsets = new int[numImages + 1]
		imageOffsets.length.times { i ->
			imageOffsets[i] = offsetSize == 2 ?
				input.readShort() & 0xffff :
				input.readInt()
		}

		// Read image headers and image data
		def lcw = new LCW()
		def rleZero = new RLEZero()

		imagesData = new ByteBuffer[numImages]
		imagesData.length.times { i ->
			def imageHeader = new ShpImageInfoDune2(input)
			def imageData = ByteBuffer.wrapNative(input.readNBytes(imageHeader.compressedSize))
			def imageSize = imageHeader.width * imageHeader.height

			// Decompress the image data
			def image = ByteBuffer.allocate(imageSize)
			imagesData[i] = imageHeader.compressed ?
				rleZero.decode(
					lcw.decode(
						imageData,
						ByteBuffer.allocateNative(imageHeader.uncompressedSize & 0xffff)
					),
					image
				) :
				rleZero.decode(imageData, image)
		}
	}

	/**
	 * Returns some information on this Dune 2 SHP file.
	 *
	 * @return Dune 2 SHP file info.
	 */
	@Override
	String toString() {

		return "SHP file (Dune 2), contains ${numImages} images of various sizes (no palette)"
	}
}
