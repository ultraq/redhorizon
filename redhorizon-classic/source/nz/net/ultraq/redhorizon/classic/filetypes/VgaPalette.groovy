/* 
 * Copyright 2019, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import nz.net.ultraq.redhorizon.filetypes.ColourFormat
import nz.net.ultraq.redhorizon.filetypes.Palette

import java.nio.ByteBuffer

/**
 * An 18-bit VGA colour palette.  VGA used 6-bits per channel (for a total of
 * 18-bits of colour), so colour byte values fall into the 0-63 range.  To match
 * modern graphics, colour values are left-shifted by 2 so they can cover 0-255.
 * 
 * @author Emanuel Rabina
 */
class VgaPalette extends Palette {

	/**
	 * Constructor, create a palette using the given data.
	 * 
	 * @param size	 Number of colours in the palette.
	 * @param format Colour format of the palette.
	 * @param bytes	 Palette data.
	 */
	VgaPalette(int size, ColourFormat format, ByteBuffer bytes) {

		super(size, format)
		size.times { i ->
			def rgb = new byte[format.value]
			bytes.get(rgb)
			palette[i] = rgb.collect { it << 2 }
		}
	}

	/**
	 * Constructor, create a palette from an input stream.
	 * 
	 * @param size	 Number of colours in the palette.
	 * @param format Colour format of the palette.
	 * @param input
	 */
	VgaPalette(int size, ColourFormat format, InputStream input) {

		super(size, format)
		size.times { i ->
			palette[i] = input.readNBytes(format.value).collect { it << 2 }
		}
	}
}
