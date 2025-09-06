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

package nz.net.ultraq.redhorizon.graphics

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
	 * The number of colour channels described by each colour.
	 */
	final int channels

	protected final byte[][] palette

	/**
	 * Constructor, build the palette parts but without any data.
	 */
	protected Palette(int size, int channels) {

		this.size = size
		this.channels = channels
		this.palette = new byte[size][channels]
	}

	/**
	 * Constructor, create a palette from a data buffer.
	 */
	Palette(int size, int channels, ByteBuffer paletteData) {

		this(size, channels)
		size.times { i ->
			palette[i] = new byte[channels]
			paletteData.get(palette[i])
		}
	}

	/**
	 * Constructor, create a palette from an inputstream.
	 */
	Palette(int size, int channels, InputStream inputStream) {

		this(size, channels)
		size.times { i ->
			palette[i] = inputStream.readNBytes(channels)
		}
	}

	/**
	 * Constructor, create a palette from an existing colour table.
	 */
	Palette(int size, int channels, byte[][] palette) {

		this.size = size
		this.channels = channels
		this.palette = palette
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
