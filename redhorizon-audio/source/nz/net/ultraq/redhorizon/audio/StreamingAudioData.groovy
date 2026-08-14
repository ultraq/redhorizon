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

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.LinkedBlockingQueue

/**
 * Multiple unknown sound buffers, used for streaming large amounts of sound
 * data.  Best suited for music tracks.
 *
 * <p>For a single-buffer variant, see {@link AudioData}.
 *
 * <p>Input streams will be decoded in a separate thread and loaded over time,
 * maintaining a buffer of up to 3 seconds (configurable in the constructor).
 * The thread which controls this object will need to call {@link #update}
 * periodically to keep converting streamed data into audio buffers.
 *
 * @author Emanuel Rabina
 */
class StreamingAudioData implements AutoCloseable, EventTarget<StreamingAudioData> {

	private static final Logger logger = LoggerFactory.getLogger(StreamingAudioData)

	private ExecutorService executor
	private Future<?> decodingTask
	private volatile BlockingQueue<SampleDecodedEvent> streamingEvents
	private int readAhead = 64
	private final List<SampleDecodedEvent> eventDrain = []
	private int buffersQueued
	private int buffersPlayed
	private final BlockingQueue<Buffer> streamedBuffers = new LinkedBlockingQueue<>()
	private boolean decodingError

	/**
	 * Constructor, set up streaming of the given input data.
	 *
	 * <p>The file extension is the hint used to determine which available
	 * {@link AudioDecoder} (registered using Java SPI) is capable of decoding the
	 * stream.
	 */
	StreamingAudioData(String fileName, InputStream inputStream) {

		this(fileName, AudioDecoder.forFileExtension(fileName.substring(fileName.lastIndexOf('.') + 1)), inputStream)
	}

	/**
	 * Constructor, set up streaming of the given input data using its name, and a
	 * selected decoder.
	 */
	StreamingAudioData(String fileName, AudioDecoder decoder, InputStream inputStream) {

		var fileSize = 0
		var duration = 0
		decoder
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
			Thread.currentThread().name = "Streaming audio ${fileName} :: Decoding"
			try {
				logger.debug('Decoding of {} started', fileName)
				var result = decoder.decode(inputStream)
				logger.debug('{} decoded after {} samples', fileName, result.buffers())
				var fileInformation = result.fileInformation()
				if (fileInformation) {
					logger.info('{}: {}', fileName, fileInformation)
				}
			}
			catch (Exception ex) {
				logger.error('Failed to decode streaming audio', ex)
				decodingError = true
			}
		}

		// Let the decode buffer fill up first
		while (streamingEvents == null || streamingEvents.remainingCapacity()) {
			Thread.onSpinWait()
		}
	}

	/**
	 * Constructor, set up streaming from an audio event source.
	 */
	StreamingAudioData(EventTarget<? extends EventTarget> audioSource, int eventCapacity) {

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

		decodingTask?.cancel(true)
		executor?.close()
		streamedBuffers*.close()
	}

	/**
	 * Load the next set of decoded sound data.  The returned buffers transfer
	 * ownership of the data to the caller, and so the caller is responsible for
	 * closing the buffers once used.
	 */
	List<Buffer> read(List<Buffer> results, int processed) {

		if (decodingError) {
			throw new IllegalStateException('An error occurred decoding the music track')
		}

		buffersPlayed += processed
		return streamedBuffers.drain(results)
	}

	/**
	 * Update the streaming data for the music track.
	 */
	void update() {

		if (decodingError) {
			throw new IllegalStateException('An error occurred decoding the music track')
		}

		// Convert decoded audio data to audio buffers
		var buffersAhead = buffersPlayed - buffersQueued + readAhead
		if (buffersAhead) {
			eventDrain.clear()
			streamedBuffers.addAll(streamingEvents.drain(eventDrain, buffersAhead).collect { event ->
				return new OpenALBuffer(event.bits(), event.channels(), event.frequency(), event.buffer())
			})
			buffersQueued += eventDrain.size()
		}
	}

	/**
	 * For signalling that the audio is ready to play when driven from an external
	 * source.
	 */
	static record PlaybackReadyEvent() implements Event {}
}
