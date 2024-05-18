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

import nz.net.ultraq.redhorizon.engine.audio.AudioRenderer
import nz.net.ultraq.redhorizon.engine.audio.AudioRequests.BufferRequest
import nz.net.ultraq.redhorizon.engine.audio.AudioRequests.SourceRequest
import nz.net.ultraq.redhorizon.engine.audio.Buffer
import nz.net.ultraq.redhorizon.engine.audio.Source
import nz.net.ultraq.redhorizon.engine.scenegraph.AudioElement
import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.engine.scenegraph.Playable
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.engine.scenegraph.SceneEvents
import nz.net.ultraq.redhorizon.engine.scenegraph.Temporal
import nz.net.ultraq.redhorizon.events.Event
import nz.net.ultraq.redhorizon.events.EventTarget
import nz.net.ultraq.redhorizon.filetypes.SoundFile
import nz.net.ultraq.redhorizon.filetypes.Streaming
import nz.net.ultraq.redhorizon.filetypes.StreamingDecoder
import nz.net.ultraq.redhorizon.filetypes.StreamingSampleEvent

import groovy.transform.TupleConstructor
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue

/**
 * An emitter of sound in the scene.
 *
 * @author Emanuel Rabina
 */
class Sound extends Node<Sound> implements AudioElement, Playable, Temporal {

	private final SoundSource soundSource

	private Source source

	/**
	 * Constructor, create a sound using data straight from a file.
	 */
	Sound(SoundFile soundFile) {

		this(soundFile.forStreaming ?
			new StreamingSoundSource(((Streaming)soundFile).streamingDecoder, true) :
			new StaticSoundSource(soundFile)
		)
	}

	/**
	 * Constructor, create a sound using any implementation of the
	 * {@link SoundSource} interface.
	 */
	Sound(SoundSource soundSource) {

		this.soundSource = soundSource
		soundSource.relay(Event, this)
	}

	@Override
	CompletableFuture<Void> onSceneAdded(Scene scene) {

		return CompletableFuture.allOf(
			scene
				.requestCreateOrGet(new SourceRequest())
				.thenAcceptAsync { newSource ->
					source = newSource
				},
			soundSource.onSceneAdded(scene)
		)
	}

	@Override
	CompletableFuture<Void> onSceneRemoved(Scene scene) {

		return CompletableFuture.allOf(
			soundSource.onSceneRemoved(scene),
			scene.requestDelete(source)
		)
	}

	@Override
	void render(AudioRenderer renderer) {

		// TODO: A lot of this should occur in an audio equivalent of the update() method
		if (source) {
			soundSource.prepareSource(renderer, source)
			renderer.updateSource(source, globalPosition)

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

	/**
	 * Interface for any source from which sound data can be obtained.
	 */
	static interface SoundSource extends EventTarget, SceneEvents {

		/**
		 * Called during {@code render}, prepare the sound source for playback.
		 */
		void prepareSource(AudioRenderer renderer, Source source)
	}

	/**
	 * A sound source using static buffers.
	 *
	 * TODO: This could be replaced w/ update() methods right?
	 */
	@TupleConstructor(defaults = false)
	static class StaticSoundSource implements SoundSource {

		final SoundFile soundFile

		private Buffer staticBuffer

		@Override
		CompletableFuture<Void> onSceneAdded(Scene scene) {

			return scene
				.requestCreateOrGet(new BufferRequest(soundFile.bits, soundFile.channels, soundFile.frequency, soundFile.soundData))
				.thenAcceptAsync { newBuffer ->
					staticBuffer = newBuffer
				}
				.thenRun { ->
					trigger(new PlaybackReadyEvent())
				}
		}

		@Override
		CompletableFuture<Void> onSceneRemoved(Scene scene) {

			return scene.requestDelete(staticBuffer)
		}

		@Override
		void prepareSource(AudioRenderer renderer, Source source) {

			if (!source.bufferAttached && staticBuffer) {
				source.attachBuffer(staticBuffer)
			}
		}
	}

	/**
	 * A sound source using streaming buffers.
	 */
	@TupleConstructor(defaults = false)
	static class StreamingSoundSource implements SoundSource {

		final StreamingDecoder streamingDecoder
		final boolean autoStream

		private final BlockingQueue<Buffer> streamingBuffers = new ArrayBlockingQueue<>(10)
		private final BlockingQueue<Buffer> streamedBuffers = new LinkedBlockingQueue<>()

		@Override
		CompletableFuture<Void> onSceneAdded(Scene scene) {

			return CompletableFuture.runAsync { ->
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

				// Run ourselves, otherwise expect the owner of this source to run this
				if (autoStream) {
					Executors.newVirtualThreadPerTaskExecutor().execute(streamingDecoder)
				}
				else {
					trigger(new StreamingReadyEvent())
				}
			}
		}

		@Override
		CompletableFuture<Void> onSceneRemoved(Scene scene) {

			streamingDecoder.cancel(true)
			return scene.requestDelete(*streamedBuffers.drain())
		}

		@Override
		void prepareSource(AudioRenderer renderer, Source source) {

			var newBuffers = streamingBuffers.drain()
			source.queueBuffers(*newBuffers)
			streamedBuffers.addAll(newBuffers)

			var buffersProcessed = source.buffersProcessed()
			if (buffersProcessed) {
				var processedBuffers = streamedBuffers.drain(buffersProcessed)
				source.unqueueBuffers(*processedBuffers)
				processedBuffers.each { processedBuffer ->
					renderer.delete(processedBuffer)
				}
			}
		}
	}
}
