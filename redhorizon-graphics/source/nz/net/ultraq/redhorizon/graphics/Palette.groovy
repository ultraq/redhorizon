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

import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLTexture
import nz.net.ultraq.redhorizon.scenegraph.Node

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.ByteBuffer

/**
 * A 24-bit colour palette for modern colour systems.
 *
 * @author Emanuel Rabina
 */
class Palette extends Node<Palette> implements AutoCloseable {

	private static final Logger logger = LoggerFactory.getLogger(Palette)

	/**
	 * The number of colours in the palette.
	 */
	final int colours

	/**
	 * The number of colour channels described by each colour.
	 */
	final int format

	final byte[][] colourData
	final Texture texture

	/**
	 * Constructor, create a palette to hold colours of the given size.
	 */
	protected Palette(int colours, int format) {

		this.colours = colours
		this.format = format
		colourData = new byte[colours][format]
	}

	/**
	 * Constructor, create a palette from a data buffer.
	 */
	Palette(int colours, int format, ByteBuffer paletteData) {

		this(colours, format)
		colours.times { i ->
			var colour = new byte[format]
			paletteData.get(colour)
			colourData[i] = colour
		}
		texture = new OpenGLTexture(colours, 1, format, paletteData)
	}

	/**
	 * Constructor, create a palette from an inputstream and the
	 * {@link PaletteDecoder} SPI.
	 */
	Palette(String fileName, InputStream inputStream) {

		this(fileName, PaletteDecoders.forFileExtension(fileName.substring(fileName.lastIndexOf('.') + 1)), inputStream)
	}

	/**
	 * Constructor, create a palette from a selected decoder operating on the
	 * input stream.
	 */
	Palette(String fileName, PaletteDecoder decoder, InputStream inputStream) {

		var result = decoder.decode(inputStream)

		var fileInformation = result.fileInformation()
		if (fileInformation) {
			logger.info('{}: {}', fileName, fileInformation)
		}

		colours = result.colours()
		format = result.format()
		colourData = result.colourData()

		var colourBuffer = ByteBuffer.allocateNative(colours * 4)
		colourData.each { colour ->
			colourBuffer.put(colour).put(1)
		}
		colourBuffer.flip()
		texture = new OpenGLTexture(colours, 1, format, colourBuffer)
	}

	@Override
	void close() {

		texture.close()
	}

	/**
	 * An overload of the {@code []} operator to get the colour data at the
	 * specified index.
	 */
	byte[] getAt(int index) {

		return colourData[index]
	}
}
