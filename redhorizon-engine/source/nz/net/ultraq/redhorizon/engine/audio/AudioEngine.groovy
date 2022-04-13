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

import nz.net.ultraq.redhorizon.engine.Engine
import nz.net.ultraq.redhorizon.engine.EngineStoppedEvent
import nz.net.ultraq.redhorizon.engine.audio.openal.OpenALContext
import nz.net.ultraq.redhorizon.engine.audio.openal.OpenALRenderer
import nz.net.ultraq.redhorizon.engine.scenegraph.ElementAddedEvent
import nz.net.ultraq.redhorizon.engine.scenegraph.ElementRemovedEvent
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.engine.scenegraph.SceneElement

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.CopyOnWriteArrayList

/**
 * Audio subsystem, manages the connection to the audio hardware and playback
 * of audio objects.
 * 
 * @author Emanuel Rabina
 */
class AudioEngine extends Engine {

	private static final Logger logger = LoggerFactory.getLogger(AudioEngine)
	private static final int TARGET_RENDER_TIME_MS = 50

	final AudioConfiguration config
	final Scene scene

	// For object lifecycles
	private final CopyOnWriteArrayList<SceneElement> addedElements = new CopyOnWriteArrayList<>()
	private final CopyOnWriteArrayList<SceneElement> removedElements = new CopyOnWriteArrayList<>()

	private boolean running

	/**
	 * Constructor, build a new engine for rendering audio.
	 * 
	 * @param config
	 * @param scene
	 */
	AudioEngine(AudioConfiguration config, Scene scene) {

		super(TARGET_RENDER_TIME_MS)
		this.config = config ?: new AudioConfiguration()
		this.scene = scene
		this.scene.on(ElementAddedEvent) { event ->
			addedElements << event.element
		}
		this.scene.on(ElementRemovedEvent) { event ->
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

		Thread.currentThread().name = 'Audio Engine'
		logger.debug('Starting audio engine')
		running = true

		// Initialization
		new OpenALContext().withCloseable { context ->
			context.withCurrent { ->
				def renderer = new OpenALRenderer(config)
				logger.debug(renderer.toString())

				// Rendering loop
				logger.debug('Audio engine in render loop...')
				engineLoop { ->

					// Initialize or delete objects which have been added/removed to/from the scene
					if (addedElements) {
						def elementsToInit = new ArrayList<SceneElement>(addedElements)
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
						def elementsToDelete = new ArrayList<SceneElement>(removedElements)
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
				}

				// Shutdown
				running = false
				logger.debug('Shutting down audio engine')
			}
		}
		logger.debug('Audio engine stopped')
		trigger(new EngineStoppedEvent())
	}

	@Override
	boolean shouldRun() {

		return running
	}

	@Override
	void stop() {

		running = false
	}
}
