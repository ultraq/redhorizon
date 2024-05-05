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

package nz.net.ultraq.redhorizon.classic.filetypes

import nz.net.ultraq.redhorizon.classic.codecs.LCW
import nz.net.ultraq.redhorizon.filetypes.ColourFormat
import nz.net.ultraq.redhorizon.filetypes.FileExtensions
import nz.net.ultraq.redhorizon.filetypes.ImageFile
import nz.net.ultraq.redhorizon.filetypes.InternalPalette
import nz.net.ultraq.redhorizon.filetypes.Palette
import nz.net.ultraq.redhorizon.filetypes.io.NativeDataInputStream
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
@FileExtensions('cps')
@SuppressWarnings('GrFinalVariableAccess')
class CpsFile implements ImageFile, InternalPalette {

	// Header constants
	// @formatter:off
	static final short COMPRESSION_NONE  = 0x0000
	static final short COMPRESSION_LZW12 = 0x0001
	static final short COMPRESSION_LZW14 = 0x0002
	static final short COMPRESSION_RLE   = 0x0003
	static final short COMPRESSION_LCW   = 0x0004

	static final int IMAGE_SIZE = 64000  // 320x200
	static final int PALETTE_SIZE = 768
	// @formatter:on

	private final NativeDataInputStream input

	// File header
	final short fileSize // Excludes the 2 bytes for this value
	final short compression
	final int imageSize
	final short paletteSize

	final int width = 320
	final int height = 200
	final ColourFormat format = FORMAT_RGB

	private Palette palette
	private ByteBuffer imageData

	/**
	 * Constructor, creates a new CPS file from data in the given input stream.
	 */
	CpsFile(InputStream inputStream) {

		input = new NativeDataInputStream(inputStream)

		// File header
		fileSize = input.readShort()

		compression = input.readShort()
		assert compression == COMPRESSION_LCW : 'Only LCW compression supported'

		imageSize = input.readInt()
		assert imageSize == IMAGE_SIZE : "CPS image size isn\'t ${IMAGE_SIZE} (320x200)"

		paletteSize = input.readShort()
		assert paletteSize == 0 || paletteSize == PALETTE_SIZE : "CPS palette size isn't 0 or 768"
	}

	@Override
	ByteBuffer getImageData() {

		if (!imageData) {
			var palette = getPalette()
			imageData = new LCW().decode(
				ByteBuffer.wrapNative(input.readNBytes((fileSize & 0xffff) - 8 - paletteSize)),
				ByteBuffer.allocateNative(imageSize)
			)
			if (palette) {
				imageData = imageData.applyPalette(palette)
			}
		}

		return imageData
	}

	@Override
	Palette getPalette() {

		// Optional palette
		if (!palette && paletteSize) {
			palette = new VgaPalette(256, FORMAT_RGB, input)
		}
		return palette
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
}
