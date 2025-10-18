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

import nz.net.ultraq.eventhorizon.Event
import nz.net.ultraq.eventhorizon.EventTarget

import groovy.transform.ImmutableOptions
import java.nio.ByteBuffer

/**
 * A class which can decode image data, the encoding of which can be found by
 * the return value of the {@link #getSupportedFileExtensions} method.
 *
 * @author Emanuel Rabina
 */
interface ImageDecoder extends EventTarget<ImageDecoder> {

	/**
	 * Perform the decoding process.  The image will be emitted in an
	 * {@link FrameDecodedEvent} and this method will block until the process is
	 * complete.
	 */
	DecodeSummary decode(InputStream inputStream)

	/**
	 * Returns the file extension commonly used by files that this decoder
	 * supports.
	 */
	String[] getSupportedFileExtensions()

	/**
	 * Event for the decoding of the header information.  This is most useful if
	 * the image data is being streamed and we need to know some information ahead
	 * of starting that.
	 */
	record HeaderDecodedEvent(int width, int height, int channels, int numFrames, float frameRate) implements Event {
		HeaderDecodedEvent(int width, int height, int channels) {
			this(width, height, channels, -1, -1f)
		}
	}

	/**
	 * Event for the streaming of a frame of image data.
	 */
	@ImmutableOptions(knownImmutables = ['data', 'palette'])
	record FrameDecodedEvent(int width, int height, int channels, ByteBuffer data, Palette palette) implements Event {
		FrameDecodedEvent(int width, int height, int channels, ByteBuffer data) {
			this(width, height, channels, data, null)
		}
	}

	/**
	 * The result of the decoding process.
	 */
	record DecodeSummary(int width, int height, int channels, int frames, String fileInformation) {
		DecodeSummary(int width, int height, int channels, int frames) {
			this(width, height, channels, frames, null)
		}
	}
}
