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

import nz.net.ultraq.redhorizon.classic.codecs.RunLengthEncoding
import nz.net.ultraq.redhorizon.classic.io.NativeDataInputStream
import nz.net.ultraq.redhorizon.graphics.ImageDecoder
import nz.net.ultraq.redhorizon.graphics.Palette

import java.nio.ByteBuffer

/**
 * An image decoder for the PCX files, which are are used for the higher
 * resolution (640x400) still images used in Red Alert and Tiberium Dawn.
 * <p>
 * PCX files can come in many flavours (8-bit, 16-bit, 24-bit, no palette, etc),
 * but for the purpose of Red Horizon, PCX files will be of the type used in the
 * Command & Conquer games: a 256-colour file with an internal palette located
 * at the tail of the file.
 *
 * @author Emanuel Rabina
 */
class PcxFileDecoder implements ImageDecoder {

	// Header constants
	// @formatter:off
	static final int  HEADER_PALETTE_SIZE  = 48
	static final byte MANUFACTURER_ZSOFT   = 0x0a // 10 = ZSoft .pcx
	static final byte VERSION_PCP25        = 0 // PC Paintbrush 2.5
	static final byte VERSION_PCP28_PAL    = 2 // PC Paintbrush 2.8 w/ palette
	static final byte VERSION_PCP28_NO_PAL = 3 // PC Paintbrush 2.8 w/o palette
	static final byte VERSION_PCP4WIN      = 4 // PC Paintbrush for Windows
	static final byte VERSION_PCPPLUS      = 5 // PC Paintbrush+
	static final byte ENCODING_RLE         = 1 // 1 = run-length encoding
	static final byte BPP_8                = 8 // 8-bits-per-pixel, 256 colours

	static final int PALETTE_COLOURS      = 256
	static final int PALETTE_CHANNELS     = 3
	static final int PALETTE_SIZE         = PALETTE_COLOURS * PALETTE_CHANNELS
	static final int PALETTE_PADDING_SIZE = 1
	// @formatter:on

	final String[] supportedFileExtensions = ['pcx']

	@Override
	DecodeSummary decode(InputStream inputStream) {

		var input = new NativeDataInputStream(inputStream)

		// File header
		var manufacturer = input.readByte()
		assert manufacturer == MANUFACTURER_ZSOFT

		var version = input.readByte()
		assert version in [VERSION_PCP25, VERSION_PCP28_PAL, VERSION_PCP28_NO_PAL, VERSION_PCP4WIN, VERSION_PCPPLUS]

		var encoding = input.readByte()
		assert encoding == ENCODING_RLE

		var bitsPerPixel = input.readByte()
		assert bitsPerPixel == BPP_8 : 'Only 8-bit (256 colour) PCX files are currently supported'

		var xMin = input.readShort()
		var yMin = input.readShort()
		var xMax = input.readShort()
		var yMax = input.readShort()
		var hdpi = input.readShort()
		var vdpi = input.readShort()

		var egaPalette = input.readNBytes(HEADER_PALETTE_SIZE)
		var reserved = input.readByte()
		var planes = input.readByte()
		var bytesPerLine = input.readShort()
		var paletteInfo = input.readShort()
		var hScreenSize = input.readShort()
		var vScreenSize = input.readShort()
		var filler = input.readNBytes(54)

		var width = xMax - xMin + 1
		var height = yMax - yMin + 1

		// Read the rest of the stream
		var imageAndPalette = input.readAllBytes()
		var encodedImage = ByteBuffer.wrapNative(imageAndPalette, 0, imageAndPalette.length - PALETTE_SIZE - PALETTE_PADDING_SIZE)

		// Build up the raw image data for use with a palette later
		// NOTE: The below is for the case when the scanline data exceeds the
		//       width/height data, but have I ever encountered that?  Otherwise
		//       this is double-handling the same data.
		var scanLines = new ArrayList<ByteBuffer>()
		var runLengthEncoding = new RunLengthEncoding((byte)0xc0)
		while (encodedImage.hasRemaining()) {
			scanLines << runLengthEncoding.decode(encodedImage, ByteBuffer.allocateNative(planes * bytesPerLine))
		}
		var indexData = ByteBuffer.allocateNative(width * height)
		(yMin..yMax).each { y ->
			var scanLine = scanLines[y]
			(xMin..xMax).each { x ->
				indexData.put(scanLine.get(x))
			}
		}
		indexData.flip()

		var paletteData = ByteBuffer.wrapNative(imageAndPalette, imageAndPalette.length - PALETTE_SIZE, PALETTE_SIZE)
		var palette = new Palette(PALETTE_COLOURS, PALETTE_CHANNELS, paletteData)

		trigger(new FrameDecodedEvent(width, height, 1, indexData, palette))

		return new DecodeSummary(width, height, 1, 1, "PCX file, ${width}x${height}, 24-bit w/ 256 colour palette")
	}
}
