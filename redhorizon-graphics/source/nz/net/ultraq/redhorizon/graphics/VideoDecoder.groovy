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

import nz.net.ultraq.eventhorizon.EventTarget
import nz.net.ultraq.redhorizon.audio.AudioDecoder.TrackInfoEvent
import nz.net.ultraq.redhorizon.graphics.ImageDecoder.ImageInfoEvent

import java.nio.file.ProviderNotFoundException

/**
 * A combination audio and video decoder for streaming video files.
 *
 * @author Emanuel Rabina
 */
interface VideoDecoder extends EventTarget<VideoDecoder> {

	/**
	 * Perform the decoding process.  Each sample of audio data will be emitted as
	 * {@code SampleDecodedEvent}s, and each frame of video data will be emitted
	 * as {@code FrameDecodedEvent}s.
	 */
	DecodeSummary decode(InputStream inputStream)

	/**
	 * Locate a video decoder for the given file extension.
	 *
	 * NOTE: The following will build fine, but IntelliJ will complain about
	 *       static interface methods until Groovy 5 support is added.
	 */
	static VideoDecoder forFileExtension(String fileExtension) {

		var serviceLoader = ServiceLoader.load(VideoDecoder)
		for (var decoder : serviceLoader) {
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
	 * Event for the decoding of the header information, so that information on
	 * the video stream can be read in advance of decoding all the video data.
	 */
	record HeaderDecodedEvent(int width, int height, int format, int frames, float frameRate,
		int bits, int channels, int frequency, long fileSize) implements ImageInfoEvent, TrackInfoEvent {}

	/**
	 * The result of the decoding process.
	 */
	record DecodeSummary(int width, int height, int format, int frames, int bits, int channels, int frequency, int buffers, String fileInformation) {
		DecodeSummary(int width, int height, int format, int frames, int bits, int channels, int frequency, int buffers) {
			this(width, height, format, frames, bits, channels, frequency, buffers, null)
		}
	}
}
