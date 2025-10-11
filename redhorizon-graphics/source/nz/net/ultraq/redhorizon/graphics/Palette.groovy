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

import org.joml.Vector4f
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.ByteBuffer

/**
 * A 24-bit colour palette for modern colour systems.
 *
 * @author Emanuel Rabina
 */
class Palette implements AutoCloseable {

	private static final Logger logger = LoggerFactory.getLogger(Palette)

	/**
	 * The number of colours in the palette.
	 */
	final int colours

	/**
	 * The number of colour channels described by each colour.
	 */
	final int channels

	final byte[][] colourData
	final Texture texture

	protected Vector4f[] asVec4s

	/**
	 * Constructor, create a palette from a data buffer.
	 */
	Palette(int colours, int channels, ByteBuffer paletteData) {

		this.colours = colours
		this.channels = channels
		colourData = new byte[colours][channels]
		colours.times { i ->
			var colour = new byte[channels]
			paletteData.get(colour)
			colourData[i] = colour
		}
		texture = new OpenGLTexture(colours, 1, channels, paletteData)
	}

	/**
	 * Constructor, create a palette from an inputstream and the
	 * {@link PaletteDecoder} SPI.
	 */
	Palette(String fileName, InputStream inputStream) {

		var result = PaletteDecoders
			.forFileExtension(fileName.substring(fileName.lastIndexOf('.') + 1))
			.decode(inputStream)

		var fileInformation = result.fileInformation()
		if (fileInformation) {
			logger.info('{}: {}', fileName, fileInformation)
		}

		colours = result.colours()
		channels = result.channels()
		colourData = result.colourData()

		var colourBuffer = ByteBuffer.allocateNative(colours * 4)
		colourData.each { colour ->
			colourBuffer.put(colour).put(1)
		}
		colourBuffer.flip()
		texture = new OpenGLTexture(colours, 1, channels, colourBuffer)
	}

	/**
	 * Coerce the palette to one of many supported types.
	 */
	Object asType(Class clazz) {

		switch (clazz) {
			case Vector4f[] -> {
				if (asVec4s == null) {
					asVec4s = new Vector4f[colours]
					colours.times { i ->
						asVec4s[i] = new Vector4f(
							(colourData[i][0] & 0xff) / 256 as float,
							(colourData[i][1] & 0xff) / 256 as float,
							(colourData[i][2] & 0xff) / 256 as float,
							1
						)
					}
				}
				yield asVec4s
			}
			default -> {
				throw new UnsupportedOperationException("Cannot coerce palette to ${clazz}")
			}
		}
	}

	@Override
	void close() {

		texture?.close()
	}

	/**
	 * An overload of the {@code []} operator to get the colour data at the
	 * specified index.
	 */
	byte[] getAt(int index) {

		return colourData[index]
	}
}
