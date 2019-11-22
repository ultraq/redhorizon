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

import nz.net.ultraq.redhorizon.engine.EngineSubsystem
import nz.net.ultraq.redhorizon.engine.AudioElement
import nz.net.ultraq.redhorizon.scenegraph.SceneElement
import static nz.net.ultraq.redhorizon.engine.audio.AudioLifecycleState.*

/**
 * Audio subsystem, manages the connection to the audio hardware and rendering
 * of audio objects.
 * 
 * @author Emanuel Rabina
 */
class AudioEngine extends EngineSubsystem {

	private static final int TARGET_RENDER_TIME_MS = 20

	private final SceneElement sceneElement

	/**
	 * Constructor, build a new audio engine for rendering the given element.
	 * 
	 * @param sceneElement
	 */
	AudioEngine(SceneElement sceneElement) {

		super(TARGET_RENDER_TIME_MS)
		this.sceneElement = sceneElement
	}

	/**
	 * Starts the audio engine loop: builds a connection to the OpenAL device,
	 * renders audio items found within the current scene, cleaning it all up when
	 * made to shut down.
	 */
	@Override
	void run() {

		Thread.currentThread().name = 'Red Horizon - Audio Engine'

		// Initialization
		new OpenALContext().withCloseable { context ->
			context.makeCurrent()

			def renderer = new OpenALRenderer()
			def audioElementStates = [:]

			// Rendering loop
			renderLoop { ->
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

			// Shutdown
			audioElementStates.keySet().each { audioElement ->
				audioElement.delete(renderer)
			}
		}

		stopLatch.countDown()
	}
}
