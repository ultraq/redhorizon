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
import nz.net.ultraq.redhorizon.scenegraph.Scene
import nz.net.ultraq.redhorizon.scenegraph.SceneElementVisitor
import static nz.net.ultraq.redhorizon.engine.audio.AudioLifecycleState.*

import groovy.transform.TupleConstructor
import java.util.concurrent.CountDownLatch

/**
 * Audio subsystem, manages the connection to the audio hardware and rendering
 * of audio objects.
 * 
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class AudioEngine implements EngineSubsystem {

	private static final int TARGET_RENDER_TIME_MS = 100

	final Scene scene

	private CountDownLatch stopLatch = new CountDownLatch(1)
	private boolean running

	/**
	 * Perform the render loop within a certain render budget, sleeping the thread
	 * if necessary to not exceed it.
	 * 
	 * @param renderLoop
	 */
	private void renderLoop(Closure renderLoop) {

		while (running) {
			def loopStart = System.currentTimeMillis()
			renderLoop()
			def loopEnd = System.currentTimeMillis()

			def renderExecutionTime = loopEnd - loopStart
			if (renderExecutionTime < TARGET_RENDER_TIME_MS) {
				Thread.sleep(TARGET_RENDER_TIME_MS - renderExecutionTime)
			}
		}
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
		def context = new OpenALContext()
		context.withCloseable { ->
			context.makeCurrent()

			def renderer = new OpenALRenderer()
			def audioElementStates = [:]
			running = true

			// Rendering loop
			renderLoop { ->
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	void stop() {

		running = false
		stopLatch.await()
	}
}
