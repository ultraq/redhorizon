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

package nz.net.ultraq.redhorizon.filetypes.bmp

import nz.net.ultraq.redhorizon.filetypes.ColourFormat
import nz.net.ultraq.redhorizon.filetypes.FileExtensions
import nz.net.ultraq.redhorizon.filetypes.ImageFile
import static nz.net.ultraq.redhorizon.filetypes.ColourFormat.FORMAT_RGB

import java.nio.ByteBuffer
import javax.imageio.ImageIO

/**
 * Wrapper of the Java implementation of Bitmap (BMP) images.
 * 
 * @author Emanuel Rabina
 */
@FileExtensions('bmp')
class BmpFile implements ImageFile {

	final int width
	final int height
	final ColourFormat format = FORMAT_RGB
	final ByteBuffer imageData

	/**
	 * Constructor, creates a BMP file from the given input stream.
	 * 
	 * @param inputStream
	 */
	BmpFile(InputStream inputStream) {

		def image = ImageIO.read(inputStream)

		width  = image.width
		height = image.height

		// Let Java do the colour space conversion, compacting the result to an RGB image buffer
		imageData = ByteBuffer.allocateNative(width * height * format.value)
		def rgbArray = image.getRGB(0, 0, width, height, null, 0, width)
		rgbArray.each { pixel ->
			imageData.put(pixel >>> 16 as byte)
			imageData.put(pixel >>> 8 as byte)
			imageData.put(pixel as byte)
		}
		imageData.rewind()
	}

	/**
	 * Returns some information on this BMP file.
	 * 
	 * @return BMP file info.
	 */
	@Override
	String toString() {

		return "BMP file, ${width}x${height}, 24-bit colour"
	}
}
