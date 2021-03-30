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
import nz.net.ultraq.redhorizon.scenegraph.SceneElement
import static nz.net.ultraq.redhorizon.engine.ElementLifecycleState.*

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Audio subsystem, manages the connection to the audio hardware and playback
 * of audio objects.
 * 
 * @author Emanuel Rabina
 */
class AudioEngine extends Engine {

	private static final Logger logger = LoggerFactory.getLogger(AudioEngine)
	private static final int TARGET_RENDER_TIME_MS = 50

	private final List<SceneElement> sceneElements = []

	/**
	 * Constructor, build a new engine for rendering audio.
	 */
	AudioEngine() {

		super(TARGET_RENDER_TIME_MS)
	}

	/**
	 * Add an element to start rendering from the next pass.
	 *
	 * @param sceneElement
	 */
	void addSceneElement(SceneElement sceneElement) {

		sceneElements << sceneElement
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

		// Initialization
		new OpenALContext(executorService).withCloseable { context ->
			context.withCurrent { ->
				def renderer = new OpenALRenderer()
				logger.debug(renderer.toString())
				def audioElementStates = [:]

				// Rendering loop
				logger.debug('Audio engine in render loop...')
				renderLoop { ->
					sceneElements.each { sceneElement ->
						sceneElement.accept { element ->
							if (element instanceof AudioElement) {

								// Register the audio element
								if (!audioElementStates[element]) {
									audioElementStates << [(element): STATE_NEW]
								}

								def elementState = audioElementStates[element]

								// Initialize the audio element
								if (elementState == STATE_NEW) {
									element.init(renderer)
									elementState = STATE_INITIALIZED
									audioElementStates << [(element): elementState]
								}

								// Render the audio element
								element.render(renderer)
							}
						}
					}
				}

				// Shutdown
				logger.debug('Shutting down audio engine')
				audioElementStates.keySet().each { audioElement ->
					audioElement.delete(renderer)
				}
			}
		}
	}
}
