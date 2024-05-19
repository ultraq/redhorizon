/*
 * Copyright 2007 Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.filetypes

import nz.net.ultraq.redhorizon.filetypes.codecs.RunLengthEncoding
import nz.net.ultraq.redhorizon.filetypes.io.NativeDataInputStream
import static nz.net.ultraq.redhorizon.filetypes.ColourFormat.FORMAT_RGB

import java.nio.ByteBuffer

/**
 * Implementation of the PCX file format.  PCX files are used for the higher
 * resolution (640x400) still images used in Red Alert and Tiberium Dawn.
 * <p>
 * PCX files can come in many flavours (8-bit, 16-bit, 24-bit, no palette, etc),
 * but for the purpose of Red Horizon, PCX files will be of the type used in the
 * Command & Conquer games: a 256-colour file with an internal palette located
 * at the tail of the file.
 *
 * @author Emanuel Rabina.
 */
@FileExtensions('pcx')
@SuppressWarnings('GrFinalVariableAccess')
class PcxFile implements ImageFile, InternalPalette {

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
	static final int PALETTE_SIZE         = PALETTE_COLOURS * FORMAT_RGB.value
	static final int PALETTE_PADDING_SIZE = 1
	// @formatter:on

	// Header data
	final byte manufacturer
	final byte version
	final byte encoding
	final byte bitsPerPixel
	final short xMin
	final short yMin
	final short xMax
	final short yMax
	final short hdpi
	final short vdpi
	final byte[] egaPalette
	final byte reserved
	final byte planes
	final short bytesPerLine
	final short paletteInfo
	final short hScreenSize
	final short vScreenSize
	final byte[] filler

	final int width
	final int height
	final ColourFormat format = FORMAT_RGB
	final ByteBuffer indexData
	final ByteBuffer imageData
	final Palette palette

	/**
	 * Constructor, creates a new PCX file from data in the given input stream.
	 *
	 * @param inputStream Input stream of the PCX file data.
	 */
	PcxFile(InputStream inputStream) {

		var input = new NativeDataInputStream(inputStream)

		// File header
		manufacturer = input.readByte()
		assert manufacturer == MANUFACTURER_ZSOFT

		version = input.readByte()
		assert version in [VERSION_PCP25, VERSION_PCP28_PAL, VERSION_PCP28_NO_PAL, VERSION_PCP4WIN, VERSION_PCPPLUS]

		encoding = input.readByte()
		assert encoding == ENCODING_RLE

		bitsPerPixel = input.readByte()
		assert bitsPerPixel == BPP_8 : 'Only 8-bit (256 colour) PCX files are currently supported'

		xMin = input.readShort()
		yMin = input.readShort()
		xMax = input.readShort()
		yMax = input.readShort()
		hdpi = input.readShort()
		vdpi = input.readShort()

		egaPalette = input.readNBytes(HEADER_PALETTE_SIZE)
		reserved = input.readByte()
		planes = input.readByte()
		bytesPerLine = input.readShort()
		paletteInfo = input.readShort()
		hScreenSize = input.readShort()
		vScreenSize = input.readShort()
		filler = input.readNBytes(54)

		width = xMax - xMin + 1
		height = yMax - yMin + 1

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
		indexData = ByteBuffer.allocateNative(width * height)
		(yMin..yMax).each { y ->
			var scanLine = scanLines[y]
			(xMin..xMax).each { x ->
				indexData.put(scanLine.get(x))
			}
		}
		indexData.flip()

		var paletteData = ByteBuffer.wrapNative(imageAndPalette, imageAndPalette.length - PALETTE_SIZE, PALETTE_SIZE)
		palette = new Palette(PALETTE_COLOURS, FORMAT_RGB, paletteData)

		// Apply palette to raw image data to create the final image
		imageData = indexData.applyPalette(palette)
	}

	/**
	 * Returns some information on this PCX file.
	 *
	 * @return PCX file info.
	 */
	@Override
	String toString() {

		return "PCX file, ${width}x${height}, 24-bit w/ 256 colour palette"
	}
}
