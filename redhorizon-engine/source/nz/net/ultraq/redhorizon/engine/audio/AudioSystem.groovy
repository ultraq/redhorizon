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

import nz.net.ultraq.redhorizon.engine.EngineStats
import nz.net.ultraq.redhorizon.engine.EngineSystem
import nz.net.ultraq.redhorizon.engine.EngineSystemType
import nz.net.ultraq.redhorizon.engine.audio.openal.OpenALContext
import nz.net.ultraq.redhorizon.engine.audio.openal.OpenALRenderer
import nz.net.ultraq.redhorizon.engine.scenegraph.AudioElement
import nz.net.ultraq.redhorizon.engine.scenegraph.Node

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.BlockingQueue
import java.util.concurrent.BrokenBarrierException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.LinkedBlockingQueue

/**
 * Audio system, manages the connection to the audio hardware and playback of
 * audio objects in the scene.
 *
 * @author Emanuel Rabina
 */
class AudioSystem extends EngineSystem implements AudioRequests {

	private static final Logger logger = LoggerFactory.getLogger(AudioSystem)

	final EngineSystemType type = EngineSystemType.RENDER

	private final AudioConfiguration config
	private final BlockingQueue<Tuple2<Request, CompletableFuture<AudioResource>>> creationRequests = new LinkedBlockingQueue<>()
	private final BlockingQueue<Tuple2<AudioResource, CompletableFuture<Void>>> deletionRequests = new LinkedBlockingQueue<>()
	private final List<Node> queryResults = []

	private OpenALContext context
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
	 * Run through all of the queued requests for the creation and deletion of
	 * graphics resources.
	 */
	private void processRequests(AudioRenderer renderer) {

		if (deletionRequests) {
			deletionRequests.drain().each { deletionRequest ->
				def (resource, future) = deletionRequest
				renderer.delete(resource)
				future.complete(null)
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
	<V extends AudioResource, R extends Request<V>> CompletableFuture<V> requestCreateOrGet(R request) {

		var future = new CompletableFuture<V>()
		creationRequests << new Tuple2(request, future)
		return future
	}

	@Override
	CompletableFuture<Void> requestDelete(AudioResource... resources) {

		return CompletableFuture.allOf(resources.collect { resource ->
			var future = new CompletableFuture<Void>()
			deletionRequests << new Tuple2(resource, future)
			return future
		})
	}

	@Override
	protected void runInit() {

		context = new OpenALContext()
		context.withCurrent { ->
			renderer = new OpenALRenderer(config)
			logger.debug(renderer.toString())
			EngineStats.instance.attachAudioRenderer(renderer)
		}
	}

	@Override
	protected void runLoop() {

		context.withCurrent { ->
			try {
				while (!Thread.interrupted()) {
					process { ->
						processRequests(renderer)

						// Run the audio elements
						// TODO: Split this out like the graphics system where we wait to
						//       be told to process audio objects instead of looping
						//       through the scene ourselves
						if (scene?.listener) {
							scene.listener.render(renderer)
							queryResults.clear()
							scene.query(scene.listener.range, queryResults).each { node ->
								if (node instanceof AudioElement) {
									node.render(renderer)
								}
							}
						}
					}
				}
			}
			catch (InterruptedException | BrokenBarrierException ignored) {
				// Do nothing
			}
		}
	}

	@Override
	protected void runShutdown() {

		context.withCurrent { ->
			renderer.close()
		}
		context.close()
	}
}
