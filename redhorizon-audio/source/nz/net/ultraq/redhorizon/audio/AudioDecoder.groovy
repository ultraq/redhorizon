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

import nz.net.ultraq.eventhorizon.Event
import nz.net.ultraq.eventhorizon.EventTarget

import groovy.transform.ImmutableOptions
import java.nio.ByteBuffer

/**
 * A class which can decode sound data, the encoding of which can be found by
 * the return value of the {@link #getSupportedFileExtensions} method.
 *
 * @author Emanuel Rabina
 */
interface AudioDecoder extends EventTarget<AudioDecoder> {

	/**
	 * Perform the decoding process.  Each sample of data will be emitted as
	 * {@link SampleDecodedEvent}s, and this method will block until the process
	 * is complete.
	 */
	DecodeSummary decode(InputStream inputStream)

	/**
	 * Returns the file extension commonly used by files that this decoder
	 * supports.
	 */
	String[] getSupportedFileExtensions()

	/**
	 * Event for the streaming of a sample of sound data.
	 */
	@ImmutableOptions(knownImmutables = ['sample'])
	record SampleDecodedEvent(int bits, int channels, int frequency, ByteBuffer sample) implements Event {}

	/**
	 * The result of the decoding process.
	 */
	record DecodeSummary(int bits, int channels, int frequency, int samples, String fileInformation) {
		DecodeSummary(int bits, int channels, int frequency, int samples) {
			this(bits, channels, frequency, samples, null)
		}
	}
}
