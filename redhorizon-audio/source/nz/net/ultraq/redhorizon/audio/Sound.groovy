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

import nz.net.ultraq.redhorizon.audio.AudioDecoder.SampleDecodedEvent
import nz.net.ultraq.redhorizon.audio.openal.OpenALBuffer
import nz.net.ultraq.redhorizon.audio.openal.OpenALSource

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.ByteBuffer

/**
 * A single buffer and source pair, used for simple cases where the sound data
 * is small and will fit in a single buffer.  Best suited for sound effects.
 *
 * @author Emanuel Rabina
 */
class Sound implements AutoCloseable {

	private static final Logger logger = LoggerFactory.getLogger(Sound)

	private final Source source
	private final Buffer buffer

	/**
	 * Constructor, sets up a new sound using its name and a stream of data.
	 *
	 * <p>The file extension is the hint used to determine which available
	 * {@link AudioDecoder} (registered using Java SPI) is capable of decoding the
	 * stream.
	 */
	Sound(String fileName, InputStream inputStream) {

		List<ByteBuffer> samples = []
		var result = AudioDecoders
			.forFileExtension(fileName.substring(fileName.lastIndexOf('.') + 1))
			.on(SampleDecodedEvent) { event ->
				samples << event.sample()
			}
			.decode(inputStream)
		while (samples.size() != result.samples()) {
			Thread.onSpinWait()
		}

		var fileInformation = result.fileInformation()
		if (fileInformation) {
			logger.info('{}: {}', fileName, fileInformation)
		}

		buffer = new OpenALBuffer(result.bits(), result.channels(), result.frequency(), ByteBuffer.fromBuffers(*samples))
		source = new OpenALSource().attachBuffer(buffer)
	}

	@Override
	void close() {

		source?.close()
		buffer?.close()
	}

	/**
	 * Return whether the sound is currently paused.
	 */
	boolean isPaused() {

		return source.isPaused()
	}

	/**
	 * Return whether the sound is currently playing.
	 */
	boolean isPlaying() {

		return source.isPlaying()
	}

	/**
	 * Return whether the sound is currently stopped.
	 */
	boolean isStopped() {

		return source.isStopped()
	}

	/**
	 * Pause the sound.
	 */
	Sound pause() {

		if (!paused) {
			source.pause()
		}
		return this
	}

	/**
	 * Play the sound.
	 */
	Sound play() {

		if (!playing) {
			source.play()
		}
		return this
	}

	/**
	 * Stop the sound.
	 */
	Sound stop() {

		if (!stopped) {
			source.stop()
		}
		return this
	}
}
