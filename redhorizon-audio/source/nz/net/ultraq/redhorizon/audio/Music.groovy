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

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

/**
 * A source backed by an unknown number of sound buffers, used for streaming
 * large amounts of sound data.  Best suited for music tracks.
 *
 * @author Emanuel Rabina
 */
class Music implements AutoCloseable {

	private final Source source
	private final AudioDecoder decoder
	private final BlockingQueue<SampleDecodedEvent> streamingEvents = new ArrayBlockingQueue<>(32)
	private final BlockingQueue<Buffer> streamedBuffers = new LinkedBlockingQueue<>()

	/**
	 * Constructor, set up a new music track using its name and a stream of data.
	 */
	Music(String fileName, InputStream inputStream) {

		source = new OpenALSource()
		decoder = AudioDecoders.forFileExtension(fileName.substring(fileName.lastIndexOf('.') + 1))
			.on(SampleDecodedEvent) { event ->
				streamingEvents << event
			}
		decoder.decode(inputStream)

		// Let the decode buffer fill up first
		while (streamingEvents.remainingCapacity() != 0) {
			Thread.onSpinWait()
		}
		update()
	}

	@Override
	void close() {

		source.stop()
		streamedBuffers*.close()
		source.close()
	}

	/**
	 * Return whether the music is currently paused.
	 */
	boolean isPaused() {

		return source.isPaused()
	}

	/**
	 * Return whether the music is currently playing.
	 */
	boolean isPlaying() {

		return source.isPlaying()
	}

	/**
	 * Return whether the music is currently stopped.
	 */
	boolean isStopped() {

		return source.isStopped()
	}

	/**
	 * Pause the music.
	 */
	Music pause() {

		if (!paused) {
			source.pause()
		}
		return this
	}

	/**
	 * Play the music.
	 */
	Music play() {

		if (!playing) {
			source.play()
		}
		return this
	}

	/**
	 * Stop the music.
	 */
	Music stop() {

		if (!stopped) {
			source.stop()
		}
		return this
	}

	/**
	 * Called within the context of the audio device thread to update the
	 * streaming data for the music track.
	 */
	void update() {

		var buffers = streamingEvents.drain().collect { event ->
			return new OpenALBuffer(event.bits(), event.channels(), event.frequency(), event.buffer())
		}
		source.queueBuffers(*buffers)
		streamedBuffers.addAll(buffers)

		if (!source.looping) {
			var buffersProcessed = source.buffersProcessed()
			if (buffersProcessed) {
				var processedBuffers = streamedBuffers.drain(buffersProcessed)
				source.unqueueBuffers(*processedBuffers)
				processedBuffers*.close()
			}
		}
	}

	/**
	 * Set whether this track loops.
	 */
	Music withLooping(boolean looping) {

		source.withLooping(looping)
		return this
	}

	/**
	 * Set the volume of the track.
	 */
	Music withVolume(float volume) {

		source.withVolume(volume)
		return this
	}
}
