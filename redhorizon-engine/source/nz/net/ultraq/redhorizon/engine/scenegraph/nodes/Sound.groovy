/*
 * Copyright 2020, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine.scenegraph.nodes

import nz.net.ultraq.redhorizon.engine.audio.AudioElement
import nz.net.ultraq.redhorizon.engine.audio.AudioRenderer
import nz.net.ultraq.redhorizon.engine.audio.AudioRequests.BufferRequest
import nz.net.ultraq.redhorizon.engine.audio.AudioRequests.SourceRequest
import nz.net.ultraq.redhorizon.engine.audio.Buffer
import nz.net.ultraq.redhorizon.engine.audio.Source
import nz.net.ultraq.redhorizon.engine.media.Playable
import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.engine.time.Temporal
import nz.net.ultraq.redhorizon.filetypes.SoundFile
import nz.net.ultraq.redhorizon.filetypes.Streaming
import nz.net.ultraq.redhorizon.filetypes.StreamingDecoder
import nz.net.ultraq.redhorizon.filetypes.StreamingSampleEvent

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue

/**
 * An emitter of sound in the scene.
 *
 * @author Emanuel Rabina
 */
class Sound extends Node<Sound> implements AudioElement, Playable, Temporal {

	SoundFile soundFile
	StreamingDecoder streamingDecoder

	private Source source
	private Buffer staticBuffer
	private final BlockingQueue<Buffer> streamingBuffers = new ArrayBlockingQueue<>(10)
	private final BlockingQueue<Buffer> streamedBuffers = new LinkedBlockingQueue<>()

	Sound(SoundFile soundFile) {

		this.soundFile = soundFile
		if (soundFile.forStreaming) {
			assert soundFile instanceof Streaming
			streamingDecoder = soundFile.streamingDecoder
		}
	}

	Sound(StreamingDecoder streamingDecoder) {

		this.streamingDecoder = streamingDecoder
	}

	@Override
	void delete(AudioRenderer renderer) {
	}

	@Override
	void init(AudioRenderer renderer) {
	}

	@Override
	void onSceneAdded(Scene scene) {

		if (!soundFile && !streamingDecoder) {
			throw new IllegalStateException('Cannot add a Sound node to a scene without a streaming or file source')
		}

		source = scene
			.requestCreateOrGet(new SourceRequest())
			.get()

		if (streamingDecoder) {
			var buffersAdded = 0
			streamingDecoder.on(StreamingSampleEvent) { event ->
				streamingBuffers << scene
					.requestCreateOrGet(new BufferRequest(event.bits, event.channels, event.frequency, event.sample))
					.get()
				buffersAdded++
				if (buffersAdded == 10) {
					trigger(new PlaybackReadyEvent())
				}
			}

			// Run ourselves, otherwise expect the source to run this
			if (soundFile) {
				Executors.newVirtualThreadPerTaskExecutor().execute(streamingDecoder)
			}
			else {
				trigger(new StreamingReadyEvent())
			}
		}
		else if (soundFile) {
			staticBuffer = scene
				.requestCreateOrGet(new BufferRequest(soundFile.bits, soundFile.channels, soundFile.frequency, soundFile.soundData))
				.get()
		}
	}

	@Override
	void onSceneRemoved(Scene scene) {

		scene.requestDelete(source)
		if (staticBuffer) {
			scene.requestDelete(staticBuffer)
		}
		if (streamedBuffers) {
			scene.requestDelete(*streamedBuffers.drain())
		}
	}

	@Override
	void render(AudioRenderer renderer) {

		if (source) {

			// Add static or streaming buffers to the source
			if (staticBuffer) {
				source.attachBuffer(staticBuffer)
			}
			else if (streamingBuffers) {
				var newBuffers = streamingBuffers.drain()
				source.queueBuffers(*newBuffers)
				streamedBuffers.addAll(newBuffers)
			}

			// Control playback
			if (playing) {
				if (source.stopped) {
					source.rewind()
					stop()
				}
				else if (!source.playing) {
					source.play()
				}
			}
			else if (paused) {
				if (!source.paused) {
					source.pause()
				}
			}
			else {
				if (source.playing) {
					source.stop()
				}
			}

			// Clean up used buffers for a streaming source
			if (streamingDecoder) {
				var buffersProcessed = source.buffersProcessed()
				if (buffersProcessed) {
					var processedBuffers = streamedBuffers.drain(buffersProcessed)
					source.unqueueBuffers(*processedBuffers)
					renderer.deleteBuffers(*processedBuffers)
				}
			}
		}
	}

	@Override
	void tick(long updatedTimeMs) {

		if (playing && currentTimeMs == updatedTimeMs) {
			pause()
		}
		else if (paused && currentTimeMs != updatedTimeMs) {
			play()
		}
		currentTimeMs = updatedTimeMs
	}
}
