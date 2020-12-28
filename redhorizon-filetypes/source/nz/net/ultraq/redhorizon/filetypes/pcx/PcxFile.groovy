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

package nz.net.ultraq.redhorizon.filetypes.pcx

import nz.net.ultraq.redhorizon.codecs.RunLengthEncoding
import nz.net.ultraq.redhorizon.filetypes.ColourFormat
import nz.net.ultraq.redhorizon.filetypes.FileExtensions
import nz.net.ultraq.redhorizon.filetypes.ImageFile
import nz.net.ultraq.redhorizon.filetypes.InternalPalette
import nz.net.ultraq.redhorizon.filetypes.Palette
import nz.net.ultraq.redhorizon.io.NativeDataInputStream
import static nz.net.ultraq.redhorizon.filetypes.ColourFormat.*

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
class PcxFile implements ImageFile, InternalPalette {

	// Header constants
	private static final int  HEADER_PALETTE_SIZE  = 48
	private static final byte MANUFACTURER_ZSOFT   = 0x0a // 10 = ZSoft .pcx
	private static final byte VERSION_PCP25        = 0 // PC Paintbrush 2.5
	private static final byte VERSION_PCP28_PAL    = 2 // PC Paintbrush 2.8 w/ palette
	private static final byte VERSION_PCP28_NO_PAL = 3 // PC Paintbrush 2.8 w/o palette
	private static final byte VERSION_PCP4WIN      = 4 // PC Paintbrush for Windows
	private static final byte VERSION_PCPPLUS      = 5 // PC Paintbrush+
	private static final byte ENCODING_RLE         = 1 // 1 = run-length encoding
	private static final byte BPP_8                = 8 // 8-bits-per-pixel, 256 colours

	private static final int PALETTE_COLOURS      = 256
	private static final int PALETTE_SIZE         = PALETTE_COLOURS * FORMAT_RGB.value
	private static final int PALETTE_PADDING_SIZE = 1

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
	final ByteBuffer imageData
	final Palette palette

	/**
	 * Constructor, creates a new PCX file from data in the given input stream.
	 * 
	 * @param inputStream Input stream of the PCX file data.
	 */
	PcxFile(InputStream inputStream) {

		def input = new NativeDataInputStream(inputStream)

		// File header
		manufacturer = input.readByte()
		assert manufacturer == MANUFACTURER_ZSOFT

		version = input.readByte()
		assert version == VERSION_PCP25 ||
		       version == VERSION_PCP28_PAL ||
		       version == VERSION_PCP28_NO_PAL ||
		       version == VERSION_PCP4WIN ||
		       version == VERSION_PCPPLUS

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

		egaPalette   = input.readNBytes(HEADER_PALETTE_SIZE)
		reserved     = input.readByte()
		planes       = input.readByte()
		bytesPerLine = input.readShort()
		paletteInfo  = input.readShort()
		hScreenSize  = input.readShort()
		vScreenSize  = input.readShort()
		filler       = input.readNBytes(54)

		width = xMax - xMin + 1
		height = yMax - yMin + 1

		// Read the rest of the stream
		def imageAndPalette = input.readAllBytes()
		def encodedImage = ByteBuffer.wrapNative(imageAndPalette, 0, imageAndPalette.length - PALETTE_SIZE - PALETTE_PADDING_SIZE)

		// Build up the raw image data for use with a palette later
		// NOTE: The below is for the case when the scanline data exceeds the
		//       width/height data, but have I ever encountered that?  Otherwise
		//       this is double-handling the same data.
		def scanLines = new ArrayList<ByteBuffer>()
		def runLengthEncoding = new RunLengthEncoding((byte)0xc0)
		while (encodedImage.hasRemaining()) {
			def scanLine = ByteBuffer.allocateNative(planes * bytesPerLine)
			runLengthEncoding.decode(encodedImage, scanLine)
			scanLines << scanLine
		}
		def rawImageData = ByteBuffer.allocateNative(width * height)
		for (def y = yMin; y <= yMax; y++) {
			def scanLine = scanLines[y]
			for (def x = xMin; x <= xMax; x++) {
				rawImageData.put(scanLine.get(x))
			}
		}
		rawImageData.rewind()

		def paletteData = ByteBuffer.wrapNative(imageAndPalette, imageAndPalette.length - PALETTE_SIZE, PALETTE_SIZE)
		palette = new Palette(PALETTE_COLOURS, FORMAT_RGB, paletteData)

		// Apply palette to raw image data to create the final image
		imageData = rawImageData.applyPalette(palette)
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
