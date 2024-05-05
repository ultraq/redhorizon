/*
 * Copyright 2022, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import nz.net.ultraq.redhorizon.classic.codecs.LCW
import nz.net.ultraq.redhorizon.filetypes.ImageFile
import nz.net.ultraq.redhorizon.filetypes.InternalPalette
import nz.net.ultraq.redhorizon.filetypes.io.FileWriter
import nz.net.ultraq.redhorizon.filetypes.io.NativeDataOutputStream
import static nz.net.ultraq.redhorizon.classic.filetypes.CpsFile.*

import groovy.transform.InheritConstructors
import java.nio.ByteBuffer

/**
 * Write CPS files to an output stream from any other image file.
 *
 * @author Emanuel Rabina
 */
@InheritConstructors
class CpsFileWriter extends FileWriter<ImageFile, Void> {

	@Override
	void write(OutputStream outputStream, Void options) {

		var output = new NativeDataOutputStream(outputStream)
		var lcw = new LCW()
		var palette = source instanceof InternalPalette ? source.palette : null

		// Encode image
		var encodedImage = lcw.encode(source.imageData, ByteBuffer.allocateNative(source.imageData.capacity()))

		// Write header
		output.writeShort(8 + encodedImage.limit()) // (Header - this value) + image
		output.writeShort(COMPRESSION_LCW)
		output.writeInt(IMAGE_SIZE)
		output.writeShort(palette ? PALETTE_SIZE : 0)

		// Write optional palette
		if (palette) {
			palette.size.times { i ->
				output.write(palette[i])
			}
		}

		// Write image data
		output.write(encodedImage.array(), 0, encodedImage.limit())
	}
}
