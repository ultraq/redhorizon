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

import groovy.transform.TupleConstructor
import java.nio.channels.ReadableByteChannel

/**
 * Interface for image file formats.
 * 
 * @author Emanuel Rabina
 */
interface ImageFile {

	/**
	 * Supported colour formats.
	 */
	@TupleConstructor
	static enum ColourFormat {

		FORMAT_INDEXED(1),
		FORMAT_RGB(3),
		FORMAT_RGBA(4)

		final int value
	}

	/**
	 * Returns the number of bytes used to represent the colour data of a single
	 * pixel.
	 * <p>
	 * If the object implements the {@link Paletted} interface, then the return
	 * value of this method is more of an expectation of the colour-depth,
	 * rather than a given.
	 * 
	 * @return The image colour format.
	 */
	ColourFormat getFormat()

	/**
	 * Returns a byte channel into the image data of the file.
	 * 
	 * @return Byte channel containing the bytes in RGB(A) order for the image.
	 */
	ReadableByteChannel getImageData()

	/**
	 * Returns the height of the image.
	 * 
	 * @return Height of the image.
	 */
	int getHeight()

	/**
	 * Returns the width of the image.
	 * 
	 * @return Width of the image.
	 */
	int getWidth()
}
