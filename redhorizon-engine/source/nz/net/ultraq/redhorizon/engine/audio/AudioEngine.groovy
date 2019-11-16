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
import nz.net.ultraq.redhorizon.scenegraph.AudioElement
import nz.net.ultraq.redhorizon.scenegraph.Scene
import nz.net.ultraq.redhorizon.scenegraph.SceneElementVisitor
import static nz.net.ultraq.redhorizon.engine.audio.AudioLifecycleState.*

import groovy.transform.TupleConstructor
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

/**
 * Audio subsystem, manages the connection to the audio hardware and rendering
 * of audio objects.
 * 
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class AudioEngine implements EngineSubsystem {

	final Scene scene

	private CountDownLatch startLatch = new CountDownLatch(1)
	private CountDownLatch stopLatch = new CountDownLatch(1)
	private boolean running

	/**
	 * Starts the audio engine loop: builds a connection to the OpenAL device,
	 * renders audio items found within the current scene, cleaning it all up when
	 * made to shut down.
	 */
	@Override
	void start() {

		def audioEngineExecutor = Executors.newCachedThreadPool()

		audioEngineExecutor.execute { ->
			Thread.currentThread().name = 'Red Horizon - Audio Engine'

			// Initialization
			def context = new OpenALContext()
			context.makeCurrent()
			context.withCloseable { ->

				def renderer = new OpenALAudioRenderer(context)
				startLatch.countDown()

				def audioElementStates = [:]
				running = true

				// Rendering loop
				while (running) {
					scene.accept { element ->
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
					} as SceneElementVisitor
				}

				// Shutdown
				audioElementStates.keySet().each { audioElement ->
					audioElement.delete(renderer)
				}
			}

			stopLatch.countDown()
		}

		startLatch.await()
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void stop() {

		running = false
		stopLatch.await()
	}
}
