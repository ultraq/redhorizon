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
import nz.net.ultraq.redhorizon.scenegraph.Scene

import groovy.transform.Canonical
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Audio subsystem, manages the connection to the audio hardware and rendering
 * of audio objects.
 * 
 * @author Emanuel Rabina
 */
class AudioSubsystem implements EngineSubsystem {

	private final Scene scene

	@Lazy
	private ExecutorService audioEngineExecutor = { Executors.newCachedThreadPool() }()
	private CountDownLatch startLatch = new CountDownLatch(1)
	private CountDownLatch stopLatch = new CountDownLatch(1)
	private boolean running

	/**
	 * Constructor, initializes the audio engine for the given scene.
	 * 
	 * @param scene
	 */
	AudioSubsystem(Scene scene) {

		this.scene = scene
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void start() {

		audioEngineExecutor.execute(new AudioEngine())
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


	/**
	 * Audio engine loop: builds a connection to the OpenAL device, renders audio
	 * items as necessary, cleans it all up when made to shut down.
	 */
	@Canonical
	private class AudioEngine implements Runnable {

		@Override
		void run() {

			Thread.currentThread().name = 'Red Horizon - Audio Engine'

			// Initialization
			def renderer = new OpenALAudioRenderer()
			renderer.initialize()
			startLatch.countDown()

			// Rendering loop
			renderer.withCloseable { ->
				running = true
				while (running) {
					scene.accept({ audioObject ->

					} as AudioRenderingVisitor)
				}
			}

			// Shutdown
			stopLatch.countDown()
		}
	}
}
