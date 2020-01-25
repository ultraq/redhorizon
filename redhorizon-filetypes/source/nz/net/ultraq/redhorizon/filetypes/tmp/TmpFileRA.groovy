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

package nz.net.ultraq.redhorizon.filetypes.tmp

import nz.net.ultraq.redhorizon.filetypes.ColourFormat
import nz.net.ultraq.redhorizon.filetypes.FileExtensions
import nz.net.ultraq.redhorizon.filetypes.ImagesFile
import nz.net.ultraq.redhorizon.io.NativeDataInputStream
import static nz.net.ultraq.redhorizon.filetypes.ColourFormat.FORMAT_RGB

import java.nio.ByteBuffer

/**
 * Representation of Red Alert's map tiles.  These are the various bits and
 * pieces which comprise the [MapPack] section of the scenario files.
 * <p>
 * I can't recall what resources I used to build this file, but as of writing a
 * combination of the following seems useful:
 * <ul>
 *   <li>https://cnc.fandom.com/wiki/Red_Alert_File_Formats_Guide (see the RMT
 *       files section</li>
 *   <li>https://web.archive.org/web/20070812201106/http://freecnc.org:80/dev/template-format/</li>
 * </ul>
 * 
 * @author Emanuel Rabina
 */
@FileExtensions(['int', 'sno', 'tmp'])
class TmpFileRA implements ImagesFile {

	// File header
	final int width     // Stored in file as short
	final int height    // Stored in file as short
	final int numImages // Stored in file as short
	final short unknown1
	final short tileWidth
	final short tileHeight
	final int fileSize
	final int imageStart
	final int unknown2
	final int unknown3
	final int index2
	final int unknown4
	final int index1

	final ColourFormat format = FORMAT_RGB
	final ByteBuffer[] imagesData

	/**
	 * Constructor, create a new RA template file from data in the input stream.
	 * 
	 * @param inputStream
	 */
	TmpFileRA(InputStream inputStream) {

		def input = new NativeDataInputStream(inputStream)

		// File header
		width = input.readShort()
		assert width == 24 : 'Tile width should be 24 pixels'

		height = input.readShort()
		assert height == 24 : 'Tile height should be 24 pixels'

		numImages  = input.readShort()
		unknown1   = input.readShort()
		tileWidth  = input.readShort()
		tileHeight = input.readShort()
		fileSize   = input.readInt()
		imageStart  = input.readInt()
		unknown2   = input.readInt()
		unknown3   = input.readInt()
		index2     = input.readInt()
		unknown4   = input.readInt()
		index1     = input.readInt()

		assert index2 - index1 == numImages

		// Image offsets and data
		def imageOffsets = new byte[numImages]
		imageOffsets.length.times { i ->
			imageOffsets[i] = input.readByte()
		}
		imagesData = new ByteBuffer[numImages]
		imagesData.length.times { i ->
			def imageOffset = imageOffsets[i]
			if (imageOffset != 0xff) {
				imagesData[i] = ByteBuffer.wrapNative(input.readNBytes(width * height))
			}
		}
	}
}
