/*
 * Copyright 2022, Emanuel Rabina (http://www.ultraq.net.nz/)
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
import nz.net.ultraq.redhorizon.filetypes.ImageFile
import nz.net.ultraq.redhorizon.filetypes.io.FileWriter
import nz.net.ultraq.redhorizon.filetypes.io.NativeDataOutputStream
import static nz.net.ultraq.redhorizon.classic.filetypes.ShpFile.*
import static nz.net.ultraq.redhorizon.filetypes.ColourFormat.FORMAT_INDEXED

import groovy.transform.InheritConstructors
import java.nio.ByteBuffer

/**
 * Write SHP files to an output stream from any single image file.
 *
 * @author Emanuel Rabina
 */
@InheritConstructors
class ShpFileWriter extends FileWriter<ImageFile, ShpFileWriterOptions> {

	@Override
	void write(OutputStream outputStream, ShpFileWriterOptions options) {

		def (width, height, numImages) = options

		// Check options for converting a single image to an SHP file are valid
		assert width < MAX_WIDTH : "Image width must be less than ${MAX_WIDTH}"
		assert height < MAX_HEIGHT : "Image height must be less than ${MAX_HEIGHT}"
		assert source.width % width == 0 : "Source file doesn't divide cleanly into ${width}x${height} images"
		assert source.height % height == 0 : "Source file doesn't divide cleanly into ${width}x${height} images"
		assert source.format == FORMAT_INDEXED : 'Source file must contain paletted image data'

		def output = new NativeDataOutputStream(outputStream)

		// Write header
		output.writeShort(numImages)
		output.writeShort(0) // x
		output.writeShort(0) // y
		output.writeShort(width)
		output.writeShort(height)
		output.writeShort(0) // delta
		output.writeShort(0) // flags

		// Split the image file data into multiple smaller images
		def imagesData = source.imageData.split(source.width, source.height, width, height)

		def lcw = new LCW()

		// Encode images
		def encodedImages = new ByteBuffer[numImages]
		numImages.each { index ->
			def imageData = imagesData[index]
			encodedImages[index] = lcw.encode(imageData, ByteBuffer.allocateNative(imageData.capacity()))
		}

		// Write images offset data
		def offsetBase = HEADER_SIZE + (OFFSET_SIZE * (numImages + 2))
		numImages.times { index ->
			output.writeInt(offsetBase | (FORMAT_LCW << 24))
			output.writeInt(0)
			offsetBase += encodedImages[index].limit()
		}
		output.writeInt(offsetBase)
		output.writeInt(0)
		output.writeInt(0)
		output.writeInt(0)

		// Write images data
		encodedImages.each { image ->
			output.write(image.array(), 0, image.limit())
		}
	}

	/**
	 * SHP file writing options.
	 */
	static record ShpFileWriterOptions(int width, int height, int numImages) {}
}
