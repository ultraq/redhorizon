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
import nz.net.ultraq.redhorizon.filetypes.SoundFile
import nz.net.ultraq.redhorizon.filetypes.Streaming
import nz.net.ultraq.redhorizon.filetypes.StreamingSampleEvent

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.transform.TupleConstructor
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.Executors

/**
 * An emitter of sound in the scene.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(includes = ['soundFile'])
class Sound implements Node<Sound>, AudioElement, Playable {

	private static final Logger logger = LoggerFactory.getLogger(Sound)

	final SoundFile soundFile

	private Source source
	private Buffer staticBuffer
	private BlockingQueue<Buffer> streamingBuffers = new ArrayBlockingQueue<>(10)
	private final List<Buffer> streamedBuffers = []

	@Override
	void delete(AudioRenderer renderer) {
	}

	@Override
	void init(AudioRenderer renderer) {
	}

	@Override
	void onSceneAdded(Scene scene) {

		source = scene
			.requestCreateOrGet(new SourceRequest())
			.get()

		var bits = soundFile.bits
		var channels = soundFile.channels
		var frequency = soundFile.frequency

		if (soundFile.forStreaming) {
			assert soundFile instanceof Streaming
			var decoder = soundFile.streamingDecoder
			decoder.on(StreamingSampleEvent) { event ->
				streamingBuffers << scene
					.requestCreateOrGet(new BufferRequest(bits, channels, frequency, event.sample))
					.get()
			}
			Executors.newVirtualThreadPerTaskExecutor().execute(decoder)

			// TODO: Delete streamed buffers to free up memory
		}
		else {
			staticBuffer = scene
				.requestCreateOrGet(new BufferRequest(bits, channels, frequency, soundFile.soundData))
				.get()
		}
	}

	@Override
	void onSceneRemoved(Scene scene) {

		scene.requestDelete(source)
		if (staticBuffer) {
			scene.requestDelete(staticBuffer)
		}
		if (streamingBuffers) {
			scene.requestDelete(*streamedBuffers)
		}
	}

	@Override
	void render(AudioRenderer renderer) {

		if (!source) {
			return
		}

		if (staticBuffer) {
			source.attachBuffer(staticBuffer)
		}
		else if (streamingBuffers) {
			var newBuffers = streamingBuffers.drain()
			source.queueBuffers(*newBuffers)
			streamedBuffers.addAll(newBuffers)
		}

		if (playing) {
			if (source.stopped) {
				logger.debug("Buffer exhausted, stopping")
				source.rewind()
				stop()
			}
			else if (!source.playing) {
				logger.debug("Playing")
				source.play()
			}
		}
		else if (paused) {
			if (!source.paused) {
				logger.debug("Pausing")
				source.pause()
			}
		}
		else {
			if (source.playing) {
				logger.debug("Stopping")
				source.stop()
			}
		}
	}
}
