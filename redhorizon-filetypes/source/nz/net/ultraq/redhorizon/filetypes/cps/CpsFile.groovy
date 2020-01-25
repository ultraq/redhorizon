/* 
 * Copyright 2007 Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.filetypes.cps

import nz.net.ultraq.redhorizon.codecs.LCW
import nz.net.ultraq.redhorizon.filetypes.ColourFormat
import nz.net.ultraq.redhorizon.filetypes.ImageFile
import nz.net.ultraq.redhorizon.filetypes.Palette
import nz.net.ultraq.redhorizon.filetypes.PalettedInternal
import nz.net.ultraq.redhorizon.filetypes.VgaPalette
import nz.net.ultraq.redhorizon.io.NativeDataInputStream
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
class CpsFile implements ImageFile, PalettedInternal {

	// Header constants
	private static final int COMPRESSION_LBM = 0x0003 // From WestPak2, don't know what this is
	private static final int COMPRESSION_LCW = 0x0004
	private static final int IMAGE_SIZE      = 64000  // 320x200
	private static final int PALETTE_SIZE    = 768

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

	// CPS constants
	private static final int IMAGE_WIDTH  = 320
	private static final int IMAGE_HEIGHT = 200

	private static final String PARAM_NOPALETTE = "-nopal"

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
	 * Constructor, creates a new cps file from another image.
	 * 
	 * @param name		Name of the CPS file.
	 * @param imagefile {@link ImageFile} instance.
	 * @param params	Additional parameters: unpaletted (opt).
	 */
//	private CpsFile(String name, ImageFile imagefile, String... params) {
//
//		super(name)
//
//		boolean usepalette = true
//
//		// Grab the parameters
//		for (String param: params) {
//			if (param.equals(PARAM_NOPALETTE)) {
//				usepalette = false
//			}
//		}
//
//		// Ensure the image meets CPS file requirements
//		if (imagefile.width() != IMAGE_WIDTH) {
//			throw new IllegalArgumentException("CPS file image size isn't 0xFA00 (320x200)")
//		}
//		if (imagefile.height() != IMAGE_HEIGHT) {
//			throw new IllegalArgumentException("CPS file image size isn't 0xFA00 (320x200)")
//		}
//
//		// Check for a palette if creating a paletted CPS
//		if (usepalette && !(imagefile instanceof PalettedInternal)) {
//			throw new IllegalArgumentException(
//					"No palette found in source image for use in paletted CPS file")
//		}
//
//		// Copy palette, image
//		cpspalette = usepalette ? new CpsPalette(((PalettedInternal)imagefile).getPalette()) : null
//		cpsimage   = BufferUtility.readRemaining(((PalettedInternal)imagefile).getRawImageData())
//	}

	/**
	 * Constructor, creates a new cps file from a pcx file.
	 * 
	 * @param name	  The name of this file.
	 * @param pcxfile PCX file to draw data from.
	 * @param params  Additional parameters: unpaletted (opt).
	 */
//	CpsFile(String name, PcxFile pcxfile, String... params) {
//
//		this(name, (ImageFile)pcxfile, params)
//	}

	/**
	 * Returns some information on this CPS file.
	 * 
	 * @return CPS file info.
	 */
	@Override
	String toString() {

		return "CPS file, ${width}x${height}, 8-bit ${palette ? 'w/ internal palette' : '(no palette)'}"
	}

	/**
	 * {@inheritDoc}
	 */
//	@Override
//	void write(GatheringByteChannel outputchannel) {
//
//		try {
//			// Encode image
//			ByteBuffer image = ByteBuffer.allocate(cpsimage.capacity())
//			CodecUtility.encodeFormat80(cpsimage, image)
//
//			// Build palette (if exists)
//			ByteBuffer palette = cpspalette != null ? cpspalette.toByteBuffer() : null
//
//			// Construct header, store to ByteBuffer
//			cpsheader = cpspalette != null ?
//					new CpsFileHeader((short)(CpsFileHeader.HEADER_SIZE + CpsFileHeader.PALETTE_SIZE + image.limit() - 2),
//							CpsFileHeader.PALETTE_SIZE) :
//					new CpsFileHeader((short)(CpsFileHeader.HEADER_SIZE + image.limit() - 2), (short)0)
//			ByteBuffer header = cpsheader.toByteBuffer()
//
//			// Write file
//			outputchannel.write(cpspalette != null ?
//					new ByteBuffer[]{ header, palette, image } :
//					new ByteBuffer[]{ header, image })
//		}
//		finally {
//			outputchannel.close()
//		}
//	}
}
