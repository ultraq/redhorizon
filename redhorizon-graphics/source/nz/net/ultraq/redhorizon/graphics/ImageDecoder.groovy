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

import groovy.transform.ImmutableOptions
import java.nio.ByteBuffer

/**
 * A class which can decode image data, the encoding of which can be found by
 * the return value of the {@link #getSupportedFileExtensions} method.
 *
 * @author Emanuel Rabina
 */
interface ImageDecoder {

	/**
	 * Perform the decoding process.
	 */
	DecodeSummary decode(InputStream inputStream)

	/**
	 * Returns the file extension commonly used by files that this decoder
	 * supports.
	 */
	String[] getSupportedFileExtensions()

	/**
	 * The result of the decoding process.
	 */
	@ImmutableOptions(knownImmutables = ['data'])
	record DecodeSummary(int width, int height, int colourChannels, ByteBuffer data, String fileInformation) {
		DecodeSummary(int width, int height, int colourChannels, ByteBuffer data) {
			this(width, height, colourChannels, data, null)
		}
	}
}
