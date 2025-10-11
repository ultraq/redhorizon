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

package nz.net.ultraq.redhorizon.graphics

/**
 * A class for decoding palette files to create {@link Palette} objects.
 *
 * @author Emanuel Rabina
 */
interface PaletteDecoder {

	/**
	 * Perform the decoding process, returning the palette and its metadata in the
	 * returned result.
	 */
	DecodeResult decode(InputStream inputStream)

	/**
	 * Returns the file extension commonly used by files that this decoder
	 * supports.
	 */
	String[] getSupportedFileExtensions()

	/**
	 * The result of the decoding process.
	 */
	record DecodeResult(int colours, int channels, byte[][] colourData, String fileInformation) {}
}
