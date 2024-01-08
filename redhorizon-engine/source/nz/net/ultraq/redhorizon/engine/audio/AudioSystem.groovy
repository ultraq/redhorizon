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

import nz.net.ultraq.redhorizon.async.RateLimitedLoop
import nz.net.ultraq.redhorizon.engine.EngineSystem
import nz.net.ultraq.redhorizon.engine.SystemReadyEvent
import nz.net.ultraq.redhorizon.engine.SystemStoppedEvent
import nz.net.ultraq.redhorizon.engine.audio.openal.OpenALContext
import nz.net.ultraq.redhorizon.engine.audio.openal.OpenALRenderer
import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.engine.scenegraph.NodeAddedEvent
import nz.net.ultraq.redhorizon.engine.scenegraph.NodeRemovedEvent
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.CopyOnWriteArrayList

/**
 * Audio system, manages the connection to the audio hardware and playback of
 * audio objects.
 *
 * @author Emanuel Rabina
 */
class AudioSystem extends EngineSystem {

	private static final Logger logger = LoggerFactory.getLogger(AudioSystem)

	final AudioConfiguration config

	// For object lifecycles
	private final CopyOnWriteArrayList<Node> addedElements = new CopyOnWriteArrayList<>()
	private final CopyOnWriteArrayList<Node> removedElements = new CopyOnWriteArrayList<>()

	@Delegate
	private RateLimitedLoop systemLoop

	/**
	 * Constructor, build a new engine for rendering audio.
	 *
	 * @param scene
	 * @param config
	 */
	AudioSystem(Scene scene, AudioConfiguration config) {

		super(scene)
		this.config = config ?: new AudioConfiguration()
		this.scene.on(NodeAddedEvent) { event ->
			addedElements << event.element
		}
		this.scene.on(NodeRemovedEvent) { event ->
			removedElements << event.element
		}
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
				def renderer = new OpenALRenderer(config)
				logger.debug(renderer.toString())
				trigger(new SystemReadyEvent())

				// Rendering loop
				logger.debug('Audio system in render loop...')
				systemLoop = new RateLimitedLoop(10, { ->

					// Initialize or delete objects which have been added/removed to/from the scene
					if (addedElements) {
						def elementsToInit = new ArrayList<Node>(addedElements)
						elementsToInit.each { elementToInit ->
							elementToInit.accept { element ->
								if (element instanceof AudioElement) {
									element.init(renderer)
								}
							}
						}
						addedElements.removeAll(elementsToInit)
					}
					if (removedElements) {
						def elementsToDelete = new ArrayList<Node>(removedElements)
						elementsToDelete.each { elementToInit ->
							elementToInit.accept { element ->
								if (element instanceof AudioElement) {
									element.delete(renderer)
								}
							}
						}
						removedElements.removeAll(elementsToDelete)
					}

					// Run the audio elements
					scene.accept { element ->
						if (element instanceof AudioElement) {
							element.render(renderer)
						}
					}
				})
				systemLoop.run()

				// Shutdown
				scene.accept { sceneElement ->
					if (sceneElement instanceof AudioElement) {
						sceneElement.delete(renderer)
					}
				}
				logger.debug('Shutting down audio system')
			}
		}
		trigger(new SystemStoppedEvent())
		logger.debug('Audio system stopped')
	}
}
