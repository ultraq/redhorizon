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
 * Locate {@link PaletteDecoder}s using Java SPI.
 *
 * @author Emanuel Rabina
 */
class PaletteDecoders {

	/**
	 * Locate a palette decoder for the given file extension.
	 *
	 * TODO: Move to the PaletteDecoder interface  when we upgrade to Groovy 5 as
	 *       that has support for static methods on interfaces.
	 */
	static PaletteDecoder forFileExtension(String fileExtension) {

		var serviceLoader = ServiceLoader.load(PaletteDecoder)
		for (var decoder in serviceLoader) {
			if (fileExtension.toLowerCase() in decoder.supportedFileExtensions) {
				return decoder
			}
		}
		throw new ProviderNotFoundException("No decoder found for file extension ${fileExtension}")
	}
}
