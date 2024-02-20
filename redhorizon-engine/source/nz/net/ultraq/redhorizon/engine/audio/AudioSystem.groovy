/*
 * Copyright 2007, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine.audio

import nz.net.ultraq.redhorizon.engine.EngineSystem
import nz.net.ultraq.redhorizon.engine.SystemReadyEvent
import nz.net.ultraq.redhorizon.engine.SystemStoppedEvent
import nz.net.ultraq.redhorizon.engine.audio.openal.OpenALContext
import nz.net.ultraq.redhorizon.engine.audio.openal.OpenALRenderer
import nz.net.ultraq.redhorizon.engine.scenegraph.AudioElement

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.BlockingQueue
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import java.util.concurrent.LinkedBlockingQueue

/**
 * Audio system, manages the connection to the audio hardware and playback of
 * audio objects.
 *
 * @author Emanuel Rabina
 */
class AudioSystem extends EngineSystem implements AudioRequests {

	private static final Logger logger = LoggerFactory.getLogger(AudioSystem)

	private final AudioConfiguration config
	private final BlockingQueue<Tuple2<Request, CompletableFuture<AudioResource>>> creationRequests = new LinkedBlockingQueue<>()
	private final BlockingQueue<AudioResource> deletionRequests = new LinkedBlockingQueue<>()

	private OpenALRenderer renderer

	/**
	 * Constructor, build a new engine for rendering audio.
	 */
	AudioSystem(AudioConfiguration config) {

		this.config = config
	}

	@Override
	void configureScene() {

		scene.audioRequestsHandler = this
	}

	/**
	 * Return the audio renderer.
	 *
	 * @return
	 */
	AudioRenderer getRenderer() {

		return renderer
	}

	/**
	 * Run through all of the queued requests for the creation and deletion of
	 * graphics resources.
	 *
	 * @param renderer
	 */
	private void processRequests(AudioRenderer renderer) {

		if (deletionRequests) {
			deletionRequests.drain().each { deletionRequest ->
				renderer.delete(deletionRequest)
			}
		}

		if (creationRequests) {
			creationRequests.drain().each { creationRequest ->
				def (request, future) = creationRequest
				var resource = switch (request) {
					case SourceRequest -> renderer.createSource()
					case BufferRequest -> renderer.createBuffer(request.bits(), request.channels(), request.frequency(), request.data())
					default -> throw new IllegalArgumentException("Cannot create resource from type ${request}")
				}
				future.complete(resource)
			}
		}
	}

	@Override
	<V extends AudioResource, R extends Request<V>> Future<V> requestCreateOrGet(R request) {

		var future = new CompletableFuture<V>()
		creationRequests << new Tuple2(request, future)
		return future
	}

	@Override
	void requestDelete(AudioResource... resources) {

		deletionRequests.addAll(resources)
	}

	/**
	 * Starts the audio engine loop: builds a connection to the OpenAL device,
	 * renders audio items found within the current scene, cleaning it all up when
	 * made to shut down.
	 */
	@Override
	void run() {

		Thread.currentThread().name = 'Audio System'
		logger.debug('Starting audio system')

		// Initialization
		new OpenALContext().withCloseable { context ->
			context.withCurrent { ->
				renderer = new OpenALRenderer(config)
				logger.debug(renderer.toString())

				if (scene) {
					configureScene()
				}
				trigger(new SystemReadyEvent())

				// Rendering loop
				logger.debug('Audio system in render loop...')
				while (!Thread.interrupted()) {
					try {
						rateLimit(100) { ->

							processRequests(renderer)

							// Run the audio elements
							scene?.accept { element ->
								if (element instanceof AudioElement) {
									element.render(renderer)
								}
							}
						}
					}
					catch (InterruptedException ignored) {
						break
					}
				}

				// Shutdown
				logger.debug('Shutting down audio system')
			}
		}
		trigger(new SystemStoppedEvent())
		logger.debug('Audio system stopped')
	}
}
