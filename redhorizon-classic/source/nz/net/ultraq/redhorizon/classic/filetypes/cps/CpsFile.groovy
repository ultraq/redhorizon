/* 
 * Copyright 2007, Emanuel Rabina (http://www.ultraq.net.nz/)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License")
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

package nz.net.ultraq.redhorizon.classic.filetypes.cps

import nz.net.ultraq.redhorizon.classic.codecs.LCW
import nz.net.ultraq.redhorizon.classic.filetypes.VgaPalette
import nz.net.ultraq.redhorizon.classic.filetypes.Writable
import nz.net.ultraq.redhorizon.filetypes.ColourFormat
import nz.net.ultraq.redhorizon.filetypes.ImageFile
import nz.net.ultraq.redhorizon.filetypes.InternalPalette
import nz.net.ultraq.redhorizon.filetypes.Palette
import nz.net.ultraq.redhorizon.io.NativeDataInputStream
import nz.net.ultraq.redhorizon.io.NativeDataOutputStream
import static nz.net.ultraq.redhorizon.filetypes.ColourFormat.FORMAT_RGB

import java.nio.ByteBuffer

/**
 * Implementation of the CPS file used in C&C and Dune 2.  The CPS file is a low
 * resolution (320x200 usually) image file that may or may not contain a
 * palette.
 * <p>
 * The CPS file is only used for the conversion utility, and does not take part
 * in the Red Horizon game.
 * 
 * @author Emanuel Rabina
 */
class CpsFile implements ImageFile, InternalPalette, Writable {

	// Header constants
	static final int COMPRESSION_LBM = 0x0003 // From WestPak2, don't know what this is
	static final int COMPRESSION_LCW = 0x0004
	static final int IMAGE_SIZE      = 64000  // 320x200
	static final int PALETTE_SIZE    = 768

	// File header
	final short fileSize
	final short compression
	final short imageSize
	final short unknown
	final short paletteSize

	final int width
	final int height
	final ColourFormat format = FORMAT_RGB
	final Palette palette
	final ByteBuffer imageData

	/**
	 * Constructor, creates a new CPS file from data in the given input stream.
	 * 
	 * @param input
	 */
	CpsFile(InputStream inputStream) {

		def input = new NativeDataInputStream(inputStream)

		// File header
		fileSize = input.readShort()

		compression = input.readShort()
		assert compression == COMPRESSION_LCW : 'Only LCW compression supported'

		imageSize = input.readShort()
		assert imageSize == IMAGE_SIZE : "CPS image size isn\'t ${IMAGE_SIZE} (320x200)"

		unknown = input.readShort()

		paletteSize = input.readShort()
		assert paletteSize == 0 || paletteSize == PALETTE_SIZE : "CPS palette size isn't 0 or 768"

		// Optional palette
		if (paletteSize) {
			palette = new VgaPalette(paletteSize, FORMAT_RGB, input)
		}

		// Image data
		imageData = ByteBuffer.allocateNative(imageSize)
		def lcw = new LCW()
		lcw.decode(imageData, ByteBuffer.wrapNative(input.readNBytes(imageSize)))
	}

	/**
	 * Constructor, creates a new CPS file from another image.
	 * 
	 * @param imageFile
	 */
	CpsFile(ImageFile imageFile) {

		assert imageFile.width == 320 : 'Source file image width must be 320 pixels'
		assert imageFile.height == 200 : 'Source file image height must be 200 pixels'

		width = imageFile.width
		height = imageFile.height
		if (imageFile instanceof InternalPalette) {
			palette = imageFile.palette
		}
		imageData = imageFile.imageData
	}

	/**
	 * Returns some information on this CPS file.
	 * 
	 * @return CPS file info.
	 */
	@Override
	String toString() {

		return "CPS file, ${width}x${height}, 8-bit ${palette ? 'w/ internal palette' : '(no palette)'}"
	}

	@Override
	void write(OutputStream outputStream) {

		def output = new NativeDataOutputStream(outputStream)
		def lcw = new LCW()

		// Encode image
		def encodedImage = ByteBuffer.allocateNative(imageData.capacity())
		lcw.encode(imageData, encodedImage)

		// Write header
		output.writeShort(8 + encodedImage.limit()) // Header + image - this value itself
		output.writeShort(COMPRESSION_LCW)
		output.writeShort(encodedImage.limit())
		output.writeShort(0)
		output.writeShort(palette ? PALETTE_SIZE : 0)

		// Write optional palette and image data
		if (palette) {
			palette.size.times { i ->
				output.write(palette[i])
			}
		}
		output.write(encodedImage.array(), 0, encodedImage.limit())
	}
}
