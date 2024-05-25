/*
 * Copyright 2007, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import nz.net.ultraq.redhorizon.filetypes.ColourFormat
import nz.net.ultraq.redhorizon.filetypes.FileExtensions
import nz.net.ultraq.redhorizon.filetypes.ImagesFile
import nz.net.ultraq.redhorizon.filetypes.io.NativeDataInputStream
import static nz.net.ultraq.redhorizon.filetypes.ColourFormat.FORMAT_INDEXED

import java.nio.ByteBuffer

/**
 * Representation of Red Alert's map tiles.  These are the various bits and
 * pieces which comprise the [MapPack] section of the scenario files.
 * <p>
 * I can't recall what resources I used to build this file, but as of writing a
 * combination of the following seems useful:
 * <ul>
 *   <li><a href="https://cnc.fandom.com/wiki/Red_Alert_File_Formats_Guide" target="_top">https://cnc.fandom.com/wiki/Red_Alert_File_Formats_Guide</a>
 *       (see the RMT files section)</li>
 *   <li><a href="https://web.archive.org/web/20070812201106/http://freecnc.org:80/dev/template-format/" target="_top">https://web.archive.org/web/20070812201106/http://freecnc.org:80/dev/template-format/</a></li>
 * </ul>
 *
 * @author Emanuel Rabina
 */
@FileExtensions(['int', 'sno', 'tem'])
class TmpFileRA implements ImagesFile {

	// File header
	final int width     // Stored in file as short
	final int height    // Stored in file as short
	final int numImages // Stored in file as short
	final short zero1
	final short tileWidth
	final short tileHeight
	final int fileSize
	final int imageOffset
	final int zero2
	final int unknown3
	final int imageIndexOffset
	final int unknown4
	final int tileIndexOffset

	final ColourFormat format = FORMAT_INDEXED

	private final NativeDataInputStream input
	private ByteBuffer[] imagesData

	/**
	 * Constructor, create a new RA template file from data in the input stream.
	 */
	TmpFileRA(InputStream inputStream) {

		input = new NativeDataInputStream(inputStream)

		// File header
		width = input.readShort()
		assert width == 24 : 'Tile width should be 24 pixels'

		height = input.readShort()
		assert height == 24 : 'Tile height should be 24 pixels'

		// @formatter:off
		numImages   = input.readShort()
		zero1       = input.readShort()
		tileWidth   = input.readShort()
		tileHeight  = input.readShort()
		fileSize    = input.readInt()
		imageOffset = input.readInt()
		zero2       = input.readInt()
		unknown3    = input.readInt()

		imageIndexOffset = input.readInt()
		unknown4         = input.readInt()
		tileIndexOffset  = input.readInt()
		// @formatter:on
	}

	@Override
	ByteBuffer[] getImagesData() {

		if (!imagesData) {
			// Image data follows the header and up to the tile index.  Not every tile
			// has an image (some are empty), so read all the image data for now and
			// spread them out according to the tile index afterwards.
			var imageBytes = ByteBuffer.wrapNative(input.readNBytes(tileIndexOffset - imageOffset))
			imagesData = new ByteBuffer[numImages]

			// Tile placement
			var imageSize = width * height
			numImages.times { i ->
				var tile = input.readByte()
				if (tile != (byte)0xff) {
					imagesData[i] = ByteBuffer.allocateNative(imageSize)
						.put(imageBytes.array(), tile * imageSize, imageSize)
						.rewind()
				}
				else {
					imagesData[i] = ByteBuffer.allocateNative(width * height)
				}
			}
		}
		return imagesData
	}

	/**
	 * Emit some information about this template file.
	 *
	 * @return
	 */
	@Override
	String toString() {

		return "TMP file (RA), contains ${numImages} ${width}x${height} images (no palette)"
	}
}
