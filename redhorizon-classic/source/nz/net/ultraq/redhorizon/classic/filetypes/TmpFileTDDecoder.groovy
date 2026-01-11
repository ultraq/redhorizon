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

import nz.net.ultraq.redhorizon.classic.io.NativeDataInputStream
import nz.net.ultraq.redhorizon.graphics.ImageDecoder

import java.nio.ByteBuffer

/**
 * Representation of Tiberium Dawn's map tiles.  These are the various bits and
 * pieces which comprise the [MapPack] section of the scenario files.
 * <p>
 * I can't recall what resources I used to build this file, but as of writing a
 * combination of the following seems useful:
 * <ul>
 *   <li><a href="https://moddingwiki.shikadi.net/wiki/Command_%26_Conquer_Tileset_Format" target="_top">https://moddingwiki.shikadi.net/wiki/Command_%26_Conquer_Tileset_Format</a></li>
 *   <li><a href="https://web.archive.org/web/20070812201106/http://freecnc.org:80/dev/template-format/" target="_top">https://web.archive.org/web/20070812201106/http://freecnc.org:80/dev/template-format/</a></li>
 * </ul>
 *
 * @author Emanuel Rabina
 */
class TmpFileTDDecoder implements ImageDecoder, FileTypeTest {

	final String[] supportedFileExtensions = ['des', 'tem', 'win']

	@Override
	DecodeSummary decode(InputStream inputStream) {

		var input = new NativeDataInputStream(inputStream)

		// File header
		var width = input.readUnsignedShort()
		assert width == 24 : 'Tile width should be 24 pixels'

		var height = input.readUnsignedShort()
		assert height == 24 : 'Tile height should be 24 pixels'

		var numImages = input.readUnsignedShort()

		var zero1 = input.readUnsignedShort()
		assert zero1 == 0

		var fileSize = input.readInt()
		var imageOffset = input.readInt()

		var zero2 = input.readInt()
		assert zero2 == 0

		var id1 = input.readShort()
		assert id1 == (short)0xffff

		var id2 = input.readShort()
		assert id2 == (short)0x0d1a

		var imageIndexOffset = input.readInt()
		var tileIndexOffset = input.readInt()

		// Image data follows the header and up to the tile index.  Not every tile
		// has an image (some are empty), so read all the image data for now and
		// spread them out according to the tile index that follows.
		var imageBytes = ByteBuffer.wrapNative(input.readNBytes(tileIndexOffset - imageOffset))

		// Tile placement
		var imageSize = width * height
		numImages.times { i ->
			var tile = input.readByte()
			if (tile != (byte)0xff) {
				var filledTile = ByteBuffer.allocateNative(imageSize)
					.put(imageBytes.array(), tile * imageSize, imageSize)
					.flip()
				trigger(new FrameDecodedEvent(width, height, 1, filledTile))
			}
			else {
				var emptyTile = ByteBuffer.allocateNative(width * height)
				trigger(new FrameDecodedEvent(width, height, 1, emptyTile))
			}
		}

		return new DecodeSummary(width, height, 1, numImages,
			"TMP file (TD), contains ${numImages} ${width}x${height} images (no palette)")
	}

	@Override
	void test(InputStream inputStream) {

		var input = new NativeDataInputStream(inputStream)

		var width = input.readUnsignedShort()
		assert width == 24 : 'Tile width should be 24 pixels'

		var height = input.readUnsignedShort()
		assert height == 24 : 'Tile height should be 24 pixels'

		input.skipBytes(2)

		var zero1 = input.readUnsignedShort()
		assert zero1 == 0

		input.skipBytes(8)

		var zero2 = input.readInt()
		assert zero2 == 0

		var id1 = input.readShort()
		assert id1 == (short)0xffff

		var id2 = input.readShort()
		assert id2 == (short)0x0d1a
	}
}
