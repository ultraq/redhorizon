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

package nz.net.ultraq.redhorizon.audio

import java.nio.file.ProviderNotFoundException

/**
 * Locate sound data decoders using Java SPI.
 *
 * @author Emanuel Rabina
 */
class AudioDecoders {

	/**
	 * Locate a sound data decoder for the given file extension.
	 *
	 * TODO: Move to the AudioDecoder interface when we upgrade to Groovy 5 as
	 *       that has support for static methods on interfaces.
	 */
	static AudioDecoder forFileExtension(String fileExtension) {

		var serviceLoader = ServiceLoader.load(AudioDecoder)
		for (AudioDecoder decoder : serviceLoader) {
			if (decoder.fileExtension().equalsIgnoreCase(fileExtension)) {
				return decoder
			}
		}
		throw new ProviderNotFoundException("No decoder found for file extension ${fileExtension}")
	}
}
