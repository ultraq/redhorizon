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

import java.nio.file.ProviderNotFoundException

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
	 * Locate a palette decoder for the given file extension.
	 *
	 * NOTE: The following will build fine, but IntelliJ will complain about
	 *       static interface methods until Groovy 5 support is added.
	 */
	static PaletteDecoder forFileExtension(String fileExtension) {

		var serviceLoader = ServiceLoader.load(PaletteDecoder)
		for (var decoder in serviceLoader) {
			if (fileExtension.toLowerCase() in decoder.getSupportedFileExtensions()) {
				return decoder
			}
		}
		throw new ProviderNotFoundException("No decoder found for file extension ${fileExtension}")
	}

	/**
	 * Returns the file extension commonly used by files that this decoder
	 * supports.
	 */
	String[] getSupportedFileExtensions()

	/**
	 * The result of the decoding process.
	 */
	record DecodeResult(int colours, int format, byte[][] colourData, String fileInformation) {}
}
