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

package nz.net.ultraq.redhorizon.classic.filedecoders

import nz.net.ultraq.redhorizon.graphics.PaletteDecoder

/**
 * A palette decoder for the PAL file format, which is a 256-color VGA palette
 * used in C&C and Dune 2.
 *
 * @author Emanuel Rabina
 */
class PalFileDecoder implements PaletteDecoder {

	private static final int COLOURS = 256
	private static final int CHANNELS = 3

	final String[] supportedFileExtensions = ['pal']

	@Override
	DecodeResult decode(InputStream inputStream) {

		var colourData = new byte[COLOURS][CHANNELS]
		COLOURS.times { i ->
			// VGA palettes used 6 bits per channel for a total of 0-63 colour values.
			// Left-shift those values by 2 to reach a 0-255 colour range.
			colourData[i] = inputStream.readNBytes(CHANNELS).collect { it << 2 }
		}
		return new DecodeResult(COLOURS, CHANNELS, colourData, 'PAL file, 256 colour VGA palette (6 bits per pixel)')
	}
}
