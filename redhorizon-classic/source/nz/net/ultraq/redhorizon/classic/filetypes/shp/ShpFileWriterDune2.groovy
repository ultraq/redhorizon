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

package nz.net.ultraq.redhorizon.classic.filetypes.shp

import nz.net.ultraq.redhorizon.classic.codecs.RLEZero
import nz.net.ultraq.redhorizon.filetypes.ImageFile
import nz.net.ultraq.redhorizon.filetypes.io.FileWriter
import nz.net.ultraq.redhorizon.filetypes.io.NativeDataOutputStream
import static nz.net.ultraq.redhorizon.classic.filetypes.shp.ShpFileDune2.*
import static nz.net.ultraq.redhorizon.filetypes.ColourFormat.FORMAT_INDEXED

import groovy.transform.InheritConstructors
import java.nio.ByteBuffer

/**
 * Write a Dune 2 SHP file from a source image.
 * 
 * @author Emanuel Rabina
 */
@InheritConstructors
class ShpFileWriterDune2 extends FileWriter<ImageFile> {

	@Override
	void write(ImageFile source, Map options) {

		def width = options.width as int
		def height = options.height as int
		def numImages = options.numImages as int

		// Check options for converting a single image to an SHP file are valid
		assert width < MAX_WIDTH : "Image width must be less than ${MAX_WIDTH}"
		assert height < MAX_HEIGHT : "Image height must be less than ${MAX_HEIGHT}"
		assert source.width % width == 0 : "Source file doesn't divide cleanly into ${width}x${height} images"
		assert source.height % height == 0 : "Source file doesn't divide cleanly into ${width}x${height} images"
		assert source.format == FORMAT_INDEXED : 'Source file must contain paletted image data'

		def widths = new int[numImages]
		Arrays.fill(widths, width)
		def heights = new int[numImages]
		Arrays.fill(heights, height)

		// Split the image file data into multiple smaller images
		def imagesData = source.imageData.splitImage(source.width, source.height, width, height)

		def rleZero = new RLEZero()
//		def lcw = new LCW()
		def preOffsetSize = (numImages + 1) * 4

		// Encode each image, update image headers, create image offsets
		def imageOffsets = ByteBuffer.allocateNative(preOffsetSize)
		int offsetTotal = preOffsetSize

		def imageHeaders = new ShpImageInfoDune2[numImages]
		def encodedImages = new ByteBuffer[numImages]
		numImages.each { index ->
			def imageData = imagesData[index]
			byte[] colourTable = null

			// If meant for faction colours, generate a colour table for the frame,
			// while at the same time replacing the image bytes with the index
			if (options.faction) {
				LinkedHashMap<Byte,Byte> colours = [:]

				// Track colour values used, replace colour values with table values
				byte tableIndex = 0
				for (def imageByteIndex = 0; imageByteIndex < imageData.limit(); imageByteIndex++) {
					def colour = imageData.get(imageByteIndex)
					if (!colours.containsKey(colour)) {
						colours.put(colour, tableIndex++)
					}
					imageData.put(imageByteIndex, colours.get(colour))
				}

				// Convert from hashmap -> byte[]
				colourTable = new byte[Math.max(colours.size(), 16)]
				def j = 0
				for (byte colour: colours.keySet()) {
					colourTable[j++] = colour
				}
			}

			// Encode image data
			// NOTE: Compression with Format80 is skipped for Dune 2 SHP files due
			//       to my implementation of Format80 compression causing "Memory
			//       Corrupts!" error messages to come from Dune 2 itself.
			def encodedImage = ByteBuffer.allocateNative(imageData.capacity() * 1.5 as int)
			rleZero.encode(imageData, encodedImage)

			// Build image header
			def imageHeader = new ShpImageInfoDune2(
				width: widths[index],
				height: heights[index],
				lookupTable: colourTable,
				compressedSize: encodedImage.limit(),
				uncompressedSize: encodedImage.limit()
			)
			imageHeaders[index] = imageHeader
			encodedImages[index] = encodedImage

			// Track offset values
			imageOffsets.putInt(offsetTotal)
			offsetTotal += imageHeader.compressedSize & 0xffff
		}

		// Add the special end-of-file offset
		imageOffsets.putInt(offsetTotal).rewind()

		def output = new NativeDataOutputStream(outputStream)

		// Write header
		output.writeShort(numImages)

		// Write offset data
		output.write(imageOffsets.array())

		// Write image headers + data
		numImages.each { index ->
			def imageHeader = imageHeaders[index]
			def encodedImage = encodedImages[index]

			output.writeShort(imageHeader.flags)
			output.write(imageHeader.slices)
			output.writeShort(imageHeader.width)
			output.write(imageHeader.height)
			output.writeShort(imageHeader.compressedSize)
			output.writeShort(imageHeader.uncompressedSize)
			output.write(encodedImage.array())
		}
	}
}
