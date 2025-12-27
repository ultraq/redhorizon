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
import nz.net.ultraq.redhorizon.audio.AudioDecoder.HeaderDecodedEvent
import nz.net.ultraq.redhorizon.audio.AudioDecoder.SampleDecodedEvent
import nz.net.ultraq.redhorizon.audio.openal.OpenALBuffer
import nz.net.ultraq.redhorizon.audio.openal.OpenALSource

import org.joml.Vector3fc
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.LinkedBlockingQueue

/**
 * A source backed by an unknown number of sound buffers, used for streaming
 * large amounts of sound data.  Best suited for music tracks.
 *
 * <p>Input streams will be decoded in a separate thread and loaded over time.
 * Whichever thread is used for updating audio will need to call {@link #update}
 * periodically to keep the music track fed.
 *
 * @author Emanuel Rabina
 */
class Music implements AutoCloseable, EventTarget<Music> {

	private static final Logger logger = LoggerFactory.getLogger(Music)

	private final Source source
	private ExecutorService executor
	private Future<?> decodingTask
	private volatile BlockingQueue<SampleDecodedEvent> streamingEvents
	private int readAhead = 64
	private final List<SampleDecodedEvent> eventDrain = []
	private int buffersQueued
	private int buffersPlayed
	private final BlockingQueue<Buffer> streamedBuffers = new LinkedBlockingQueue<>()
	private final List<Buffer> bufferDrain = []
	private boolean decodingError

	/**
	 * Constructor, set up streaming of the given music track.
	 */
	Music(String fileName, InputStream inputStream) {

		source = new OpenALSource()
		var fileSize = 0
		var duration = 0
		var decoder = AudioDecoders.forFileExtension(fileName.substring(fileName.lastIndexOf('.') + 1))
			.on(HeaderDecodedEvent) { event ->
				var bits = event.bits()
				var channels = event.channels()
				var frequency = event.frequency()
				fileSize = event.fileSize()
				if (fileSize) {
					duration = fileSize / (frequency * channels * (bits / 8)) as int
					logger.debug('Estimated track duration: {}:{}', duration / 60 as int, duration % 60 as int)
				}
			}
			.on(SampleDecodedEvent) { event ->
				if (streamingEvents == null) {
					if (fileSize && duration) {
						readAhead = (fileSize / duration / event.buffer().capacity()) * 3 as int
					}
					logger.debug('Read-ahead of {} chunks', readAhead)
					streamingEvents = new ArrayBlockingQueue<>(readAhead)
				}
				streamingEvents << event
			}
		executor = Executors.newSingleThreadExecutor()
		decodingTask = executor.submit { ->
			Thread.currentThread().name = "Music track ${fileName} :: Decoding"
			try {
				logger.debug('Music decoding of {} started', fileName)
				var result = decoder.decode(inputStream)
				logger.debug('{} decoded after {} samples', fileName, result.buffers())
				var fileInformation = result.fileInformation()
				if (fileInformation) {
					logger.info('{}: {}', fileName, fileInformation)
				}
			}
			catch (Exception ex) {
				logger.error('Failed to decode music track', ex)
				decodingError = true
			}
		}

		// Let the decode buffer fill up first
		while (streamingEvents == null || streamingEvents.remainingCapacity()) {
			Thread.onSpinWait()
		}
		update()
	}

	/**
	 * Constructor, set up streaming from an audio event source.
	 */
	Music(EventTarget<? extends EventTarget> audioSource, int eventCapacity) {

		source = new OpenALSource()
		streamingEvents = new ArrayBlockingQueue<>(eventCapacity)
		readAhead = eventCapacity

		var playbackReadyTriggered = false
		audioSource.on(SampleDecodedEvent) { event ->
			streamingEvents << event
			if (!streamingEvents.remainingCapacity() && !playbackReadyTriggered) {
				trigger(new PlaybackReadyEvent())
				playbackReadyTriggered = true
			}
		}
	}

	@Override
	void close() {

		decodingTask.cancel(true)
		executor.close()
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
	 * Continue playback of the music track.
	 */
	void render(Vector3fc position) {

		source.setPosition(position)
	}

	/**
	 * Stop the music.
	 */
	Music stop() {

		if (!stopped) {
			source.stop()
			decodingTask?.cancel(true)
		}
		return this
	}

	/**
	 * Update the streaming data for the music track.
	 */
	void update() {

		if (decodingError) {
			throw new IllegalStateException('An error occurred decoding the music track')
		}

		// Buffer the music
		var buffersAhead = !source.looping ? buffersPlayed - buffersQueued + readAhead : readAhead
		if (buffersAhead > 0) {
			eventDrain.clear()
			streamingEvents.drain(eventDrain, buffersAhead).each { event ->
				var buffer = new OpenALBuffer(event.bits(), event.channels(), event.frequency(), event.buffer())
				source.queueBuffers(buffer)
				streamedBuffers << buffer
			}
			buffersQueued += buffersAhead
		}

		// Close any used buffers (n/a for looping tracks)
		var buffersProcessed = source.buffersProcessed()
		if (buffersProcessed) {
			buffersPlayed += buffersProcessed
			bufferDrain.clear()
			streamedBuffers.drain(bufferDrain, buffersProcessed).each { buffer ->
				source.unqueueBuffers(buffer)
				buffer.close()
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

	/**
	 * For signalling that the animation is ready to play when driven from an
	 * external source.
	 */
	static record PlaybackReadyEvent() implements Event {}
}
