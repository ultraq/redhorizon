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

import java.nio.ByteBuffer

/**
 * A 24-bit colour palette for modern colour systems.
 * 
 * @author Emanuel Rabina
 */
class Palette {

	/**
	 * The number of colours in the palette.
	 */
	final int size

	/**
	 * Colour format used by this palette.
	 */
	final ColourFormat format

	protected final byte[][] palette

	/**
	 * Constructor, build the palette parts but without any data.
	 * 
	 * @param size
	 * @param format
	 */
	protected Palette(int size, ColourFormat format) {

		this.size    = size
		this.format  = format
		this.palette = new byte[size][format.value]
	}

	/**
	 * Constructor, create a palette using the given data.
	 * 
	 * @param size	 Number of colours in the palette.
	 * @param format Colour format of the palette.
	 * @param bytes	 Palette data.
	 */
	Palette(int size, ColourFormat format, ByteBuffer bytes) {

		this(size, format)
		size.times { i ->
			palette[i] = new byte[format.value]
			bytes.get(palette[i])
		}
	}

	/**
	 * Constructor, create a palette from an input stream.
	 * 
	 * @param size	 Number of colours in the palette.
	 * @param format Colour format of the palette.
	 * @param input
	 */
	Palette(int size, ColourFormat format, InputStream input) {

		this(size, format)
		size.times { i ->
			palette[i] = input.readNBytes(format.value)
		}
	}

	/**
	 * Return the colour data at the specified index.
	 * 
	 * @param index Position in the palette.
	 * @return {@code byte} values representing the requested colour.
	 */
	byte[] getAt(int index) {

		return palette[index]
	}
}
