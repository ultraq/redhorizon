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

import nz.net.ultraq.redhorizon.filetypes.FileExtensions
import nz.net.ultraq.redhorizon.filetypes.ImageFile
import nz.net.ultraq.redhorizon.filetypes.PalettedInternal
import nz.net.ultraq.redhorizon.filetypes.Palette

import java.nio.ByteBuffer
import java.nio.channels.ReadableByteChannel

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
class PcxFile implements ImageFile, PalettedInternal {

	private static final int PALETTE_SIZE         = 768
	private static final int PALETTE_PADDING_SIZE = 1

	private PcxFileHeader pcxheader
	private PcxPalette pcxpalette
	private ByteBuffer pcximage

	/**
	 * Constructor, creates a new pcx file with the given file name and file
	 * data..
	 * 
	 * @param name		  Name of the pcx file.
	 * @param bytechannel Input stream of the pcx file data.
	 */
	PcxFile(String name, ReadableByteChannel bytechannel) {

		super(name)

		try {
			// Read header
			ByteBuffer headerbytes = ByteBuffer.allocate(PcxFileHeader.HEADER_SIZE)
			bytechannel.read(headerbytes)
			headerbytes.rewind()
			pcxheader  = new PcxFileHeader(headerbytes)

			// Read the rest of the stream
			ByteBuffer pcxdata = BufferUtility.readRemaining(bytechannel)

			// Decode PCX run-length encoded image data scanline-by-scanline
			ByteBuffer sourcebytes = ByteBuffer.allocate(pcxdata.limit() - PALETTE_SIZE)
			ArrayList<ByteBuffer> scanlines = new ArrayList<>()

			while (sourcebytes.hasRemaining()) {
				ByteBuffer scanline = ByteBuffer.allocate(pcxheader.planes * pcxheader.bytesperline)
				CodecUtility.decodeRLE67(sourcebytes, scanline)
				scanlines.add(scanline)
			}

			// Cull the image to the appropriate width/height dimensions (for when
			// scanlines extend beyond the image borders)
			pcximage = ByteBuffer.allocate(width() * height())
			for (int y = pcxheader.ymin y <= pcxheader.ymax y++) {
				ByteBuffer scanline = scanlines.get(y)
				for (int x = pcxheader.xmin x <= pcxheader.xmax x++) {
					pcximage.put(scanline.get(x))
				}
			}
			pcximage.rewind()

			// Assign palette (from tail of file, after the padding byte)
			ByteBuffer palettebytes = ByteBuffer.allocate(PALETTE_SIZE)
			palettebytes.put((ByteBuffer)pcxdata.position(pcxdata.position() + PALETTE_PADDING_SIZE))
			pcxpalette = new PcxPalette(palettebytes)
		}
		finally {
			bytechannel.close()
		}
	}

	/**
	 * Does nothing.
	 */
	@Override
	void close() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	ColourFormat format() {

		return FORMAT_RGB
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	ReadableByteChannel getImageData() {

		// Apply internal palette
		ByteBuffer rgbimage = ByteBuffer.allocate(width() * height() * format().size)
		ImageUtility.applyPalette(pcximage, rgbimage, pcxpalette)
		return new ReadableByteChannelAdapter(rgbimage)
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	Palette getPalette() {

		return pcxpalette
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	ReadableByteChannel getRawImageData() {

		return pcxpalette != null ? new ReadableByteChannelAdapter(pcximage) : null
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	int height() {

		return pcxheader.ymax - pcxheader.ymin + 1
	}

	/**
	 * Returns some information on this PCX file.
	 * 
	 * @return PCX file info.
	 */
	@Override
	String toString() {

		return filename + " (PCX file)" +
			"\n  Image width: " + width() +
			"\n  Image height: " + height() +
			"\n  Colour depth: 8-bit " + (pcxpalette != null ? "(using internal palette)" : "")
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	int width() {

		return pcxheader.xmax - pcxheader.xmin + 1
	}
}
