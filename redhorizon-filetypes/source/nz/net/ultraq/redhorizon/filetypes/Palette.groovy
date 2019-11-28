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
 * A basic colour palette.
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
	 * Constructor, copy an existing palette into this one.
	 * 
	 * @param palette
	 */
//	protected AbstractPalette(Palette palette) {
//
//		size   = palette.size
//		format = palette.format
//		this.palette = new byte[size][format.value]
//		(0..<size).each { i ->
//			this.palette[i] = palette.getColour(i)
//		}
//	}

	/**
	 * Constructor, create a palette from a palette file.
	 * 
	 * @param palettefile
	 */
//	protected AbstractPalette(PaletteFile palettefile) {
//
//		this.size = palettefile.size()
//		this.format = palettefile.format()
//		this.palette = new byte[size][format.size]
//
//		try (ReadableByteChannel palettedata = palettefile.getPaletteData()) {
//			for (int i = 0 i < size i++) {
//				ByteBuffer colourbytes = ByteBuffer.allocate(format.size)
//				palettedata.read(colourbytes)
//				palette[i] = colourbytes.array()
//			}
//		}
//		// TODO: Should be able to soften the auto-close without needing this
//		catch (IOException ex) {
//			throw new RuntimeException(ex)
//		}
//	}

	/**
	 * Constructor, create a palette using the given data.
	 * 
	 * @param size	 Number of colours in the palette
	 * @param format Colour format of the palette
	 * @param bytes	 Palette data.
	 */
//	protected AbstractPalette(int size, ColourFormat format, byte[][] bytes) {
//
//		this.size    = size
//		this.format  = format
//		this.palette = bytes
//	}

	/**
	 * Constructor, create a palette using the given data.
	 * 
	 * @param size	 Number of colours in the palette.
	 * @param format Colour format of the palette.
	 * @param bytes	 Palette data.
	 */
	Palette(int size, ColourFormat format, ByteBuffer bytes) {

		this.size    = size
		this.format  = format
		this.palette = new byte[size][format.value]
		(0..<size).each { i ->
			palette[i] = new byte[format.value]
			bytes.get(palette[i])
		}
	}

	/**
	 * Constructor, create a palette using the given data.
	 * 
	 * @param size		  Number of colours in the palette.
	 * @param format	  Colour format of the palette.
	 * @param bytechannel Palette data.
	 */
//	protected AbstractPalette(int size, ColourFormat format, ReadableByteChannel bytechannel) {
//
//		this.size    = size
//		this.format  = format
//		this.palette = new byte[size][format.size]
//		for (int i = 0 i < palette.length i++) {
//			ByteBuffer colourbytes = ByteBuffer.allocate(format.size)
//			bytechannel.read(colourbytes)
//			palette[i] = colourbytes.array()
//		}
//	}

	/**
	 * Return the colour data at the specified index.
	 * 
	 * @param index Position in the palette.
	 * @return <tt>byte</tt> array of the RGB(A) values of the requested colour.
	 */
	byte[] getAt(int index) {

		return palette[index]
	}
}
