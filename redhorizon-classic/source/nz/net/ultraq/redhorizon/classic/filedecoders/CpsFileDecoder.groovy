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

package nz.net.ultraq.redhorizon.classic.filedecoders

import nz.net.ultraq.redhorizon.classic.codecs.LCW
import nz.net.ultraq.redhorizon.classic.filetypes.VgaPalette
import nz.net.ultraq.redhorizon.filetypes.io.NativeDataInputStream
import nz.net.ultraq.redhorizon.graphics.ImageDecoder
import nz.net.ultraq.redhorizon.graphics.Palette

import java.nio.ByteBuffer

/**
 * An image decoder for CPS files, which are a low-resolution image format
 * (320x200 usuall) used in C&C and Dune 2.
 *
 * @author Emanuel Rabina
 */
class CpsFileDecoder implements ImageDecoder {

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

	static final int IMAGE_WIDTH = 320
	static final int IMAGE_HEIGHT = 200
	static final int IMAGE_CHANNELS = 1
	static final int PALETTE_COLOURS = 256
	static final int PALETTE_CHANNELS = 3

	final String[] supportedFileExtensions = ['cps']

	@Override
	DecodeSummary decode(InputStream inputStream) {

		var input = new NativeDataInputStream(inputStream)

		// File header
		var fileSize = input.readShort()

		var compression = input.readShort()
		assert compression == COMPRESSION_LCW : 'Only LCW compression supported'

		var imageSize = input.readInt()
		assert imageSize == IMAGE_SIZE : "CPS image size isn\'t ${IMAGE_SIZE} (320x200)"

		var paletteSize = input.readShort()
		assert paletteSize == 0 || paletteSize == PALETTE_SIZE : "CPS palette size isn't 0 or 768"

		// Optional palette
		Palette palette = null
		if (paletteSize) {
			palette = new VgaPalette(PALETTE_COLOURS, PALETTE_CHANNELS, input)
		}

		// Indexed image data
		var source = ByteBuffer.wrapNative(input.readNBytes((fileSize & 0xffff) - 8 - paletteSize))
		var dest = ByteBuffer.allocateNative(imageSize)
		var indexData = new LCW().decode(source, dest).flipVertical(IMAGE_WIDTH, IMAGE_HEIGHT, IMAGE_CHANNELS)

		trigger(new FrameDecodedEvent(IMAGE_WIDTH, IMAGE_HEIGHT, IMAGE_CHANNELS, indexData, palette))

		return new DecodeSummary(IMAGE_WIDTH, IMAGE_HEIGHT, IMAGE_CHANNELS, 1,
			"CPS file, ${IMAGE_WIDTH}x${IMAGE_HEIGHT}, 8-bit ${palette ? 'w/ internal palette' : '(no palette)'}")
	}
}
