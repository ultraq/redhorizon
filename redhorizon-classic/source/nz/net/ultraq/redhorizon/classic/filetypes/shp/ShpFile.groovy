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

package nz.net.ultraq.redhorizon.classic.filetypes.shp

import nz.net.ultraq.redhorizon.classic.codecs.LCW
import nz.net.ultraq.redhorizon.classic.codecs.XORDelta
import nz.net.ultraq.redhorizon.classic.filetypes.Writable
import nz.net.ultraq.redhorizon.filetypes.ColourFormat
import nz.net.ultraq.redhorizon.filetypes.FileExtensions
import nz.net.ultraq.redhorizon.filetypes.ImageFile
import nz.net.ultraq.redhorizon.filetypes.ImagesFile
import nz.net.ultraq.redhorizon.io.NativeDataInputStream
import nz.net.ultraq.redhorizon.io.NativeDataOutputStream
import static nz.net.ultraq.redhorizon.filetypes.ColourFormat.FORMAT_INDEXED

import java.nio.ByteBuffer

/**
 * Implementation of the SHP file format as used in the orignal Command &
 * Conquer and Red Alert.  SHP files (shapes?) are a series of images used for
 * the visuals of a game object, like a unit or structure.
 * <p>
 * For more information about the C&C SHP file, see: http://vladan.bato.net/cnc/ccfiles4.txt
 * 
 * @author Emanuel Rabina
 */
@FileExtensions('shp')
class ShpFile implements ImagesFile, Writable {

	private static final int HEADER_SIZE = 14
	private static final int OFFSET_SIZE = 8
	private static final int MAX_WIDTH = 65535
	private static final int MAX_HEIGHT = 65535

	// Header flags
	private static final byte FORMAT_LCW       = (byte)0x80
	private static final byte FORMAT_XOR_BASE  = (byte)0x40
	private static final byte FORMAT_XOR_CHAIN = (byte)0x20

	// File header
	final int numImages // Stored in file as short
	final short x
	final short y
	final int width     // Stored in file as short
	final int height    // Stored in file as short
	final short delta
	final short flags

	final ColourFormat format = FORMAT_INDEXED

	final ShpImageInfo[] imageOffsets
	final ByteBuffer[] imagesData

	/**
	 * Constructor, creates a new SHP file from the given file data.
	 * 
	 * @param input
	 */
	ShpFile(InputStream inputStream) {

		def input = new NativeDataInputStream(inputStream)

		// File header
		numImages = input.readShort()
		x         = input.readShort()
		y         = input.readShort()
		width     = input.readShort()
		height    = input.readShort()
		delta     = input.readShort()
		flags     = input.readShort()

		// numImages() + 2 for the 0 offset and EOF pointer
		imageOffsets = new ShpImageInfo[numImages + 2]
		imageOffsets.length.times { i ->
			imageOffsets[i] = new ShpImageInfo(input)
		}

		// Decompresses the raw SHP data into palette-index data
		def lcw = new LCW()
		def xorDelta = new XORDelta(delta)

		imagesData = new ByteBuffer[numImages]
		imagesData.length.times { i ->
			def imageOffset = imageOffsets[i]

			// Format conversion buffers
			def compressedImageSize = imageOffsets[i + 1].offset - imageOffset.offset
			def compressedImage = ByteBuffer.wrapNative(input.readNBytes(compressedImageSize))

			def dest = ByteBuffer.allocateNative(width * height)

			switch (imageOffset.offsetFormat) {
				case FORMAT_LCW:
					lcw.decode(compressedImage, dest)
					break
				case FORMAT_XOR_BASE:
					xorDelta
						.deltaSource(imagesData[imageOffsets.findIndexOf { it.offset == imageOffset.refOff }])
						.decode(compressedImage, dest)
					break
				case FORMAT_XOR_CHAIN:
					xorDelta
//						.deltaSource(imagesData[i - 1])
						.decode(compressedImage, dest)
					break
			}

			imagesData[i] = dest
		}
	}

	/**
	 * Constructor, create a new SHP file from a single image.
	 * 
	 * @param imageFile
	 * @param width
	 * @param height
	 * @param numImages
	 */
	ShpFile(ImageFile imageFile, int width, int height, int numImages) {

		assert width < MAX_WIDTH : "Image width must be less than ${MAX_WIDTH}"
		assert height < MAX_HEIGHT : "Image height must be less than ${MAX_HEIGHT}"
		assert imageFile.width % width == 0 : "Source file doesn't divide cleanly into ${width}x${height} images"
		assert imageFile.height % height == 0 : "Source file doesn't divide cleanly into ${width}x${height} images"
		assert imageFile.format == FORMAT_INDEXED : 'Source file must contain paletted image data'

		this.width = width
		this.height = height
		this.numImages = numImages

		// Split the image file data into multiple smaller images
		def imageSize = width * height
		imagesData = new ByteBuffer[numImages].collect { ->
			return ByteBuffer.allocateNative(imageSize)
		}

		// TODO: What's happening here is similar to the single-loop-for-buffer code
		//       in the VqaFileWorker.  See if we can't figure a way to generalize
		//       this?
		def sourceData = imageFile.imageData
		def imagesAcross = imageFile.width / width
		for (def pointer = 0; pointer < imageSize; pointer += width) {
			def frame = (pointer / imagesAcross as int) * imageFile.width + (pointer * width)

			// Fill the target frame with 1 row from the current pointer
			imagesData[frame].put(sourceData, width)
		}
		imagesData.each { imageData ->
			imageData.rewind()
		}
		sourceData.rewind()
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

	@Override
	void writeTo(OutputStream outputStream) {

		def output = new NativeDataOutputStream(outputStream)
		def lcw = new LCW()

		// Encode images
		def images = new ByteBuffer[numImages]
		numImages.each { index ->
			def rawImage = imagesData[index]
			images[index] = lcw.encode(rawImage, ByteBuffer.allocateNative(rawImage.capacity()))
		}

		// Write header
		output.writeShort(numImages)
		output.writeShort(x)
		output.writeShort(y)
		output.writeShort(width)
		output.writeShort(height)
		output.writeShort(delta)
		output.writeShort(flags)

		// Write offset data
		def offsetBase = HEADER_SIZE + (OFFSET_SIZE * (numImages + 2))
		numImages.times { index ->
			output.writeInt(offsetBase | (FORMAT_LCW << 24))
			output.writeInt(0)
			offsetBase += images[index].limit()
		}
		output.writeInt(offsetBase)
		output.writeInt(0)
		output.writeInt(0)
		output.writeInt(0)

		// Write images data
		images.each { image ->
			output.write(image.array(), 0, image.limit())
		}
	}
}
