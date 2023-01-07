/* 
 * Copyright 2021, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine

import nz.net.ultraq.redhorizon.engine.audio.AudioConfiguration
import nz.net.ultraq.redhorizon.engine.audio.AudioEngine
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsEngine
import nz.net.ultraq.redhorizon.engine.graphics.WindowCreatedEvent
import nz.net.ultraq.redhorizon.engine.graphics.imgui.GuiEvent
import nz.net.ultraq.redhorizon.engine.input.InputEventStream
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import static nz.net.ultraq.redhorizon.engine.graphics.imgui.GuiEvent.EVENT_TYPE_STOP

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore

/**
 * A base for developing an application that uses the Red Horizon engine, this
 * class sets up the engine components and provides access to those and the
 * scene the engine was created to render.
 *
 * @author Emanuel Rabina
 */
abstract class Application {

	private static final Logger logger = LoggerFactory.getLogger(Application)

	final String windowTitle
	final AudioConfiguration audioConfig
	final GraphicsConfiguration graphicsConfig

	protected AudioEngine audioEngine
	protected GameClock gameClock
	protected GraphicsEngine graphicsEngine
	protected InputEventStream inputEventStream
	protected Scene scene = new Scene()
	protected boolean applicationStopped

	private final ExecutorService executorService = Executors.newCachedThreadPool()
	private final Semaphore applicationStoppingSemaphore = new Semaphore(1)

	/**
	 * Constructor, creates an application with the title suffix and
	 * configuration.
	 *
	 * @param windowTitle
	 * @param audioConfig
	 * @param graphicsConfig
	 */
	protected Application(String windowTitle, AudioConfiguration audioConfig, GraphicsConfiguration graphicsConfig) {

		this.windowTitle = windowTitle ? "Red Horizon - ${windowTitle}" : null
		this.audioConfig = audioConfig
		this.graphicsConfig = graphicsConfig
	}

	/**
	 * Called when all the application setup has completed and it's time for the
	 * application itself to run.
	 */
	protected void applicationStart() {
	}

	/**
	 * Called when the application is stopping and before the engines are shut
	 * down.  Ideal for performing any cleanup tasks.
	 */
	protected void applicationStop() {
	}

	/**
	 * Start the application.
	 */
	final void start() {

		logger.debug('Initializing application...')

		// Start the engines
		inputEventStream = new InputEventStream()
		inputEventStream.on(GuiEvent) { event ->
			if (event.type == EVENT_TYPE_STOP) {
				stop()
			}
		}

		var applicationReady = new CountDownLatch(2)

		audioEngine = new AudioEngine(audioConfig, scene)
		audioEngine.on(EngineLoopStartEvent) { event ->
			applicationReady.countDown()
		}
		audioEngine.on(EngineStoppedEvent) { event ->
			stop()
		}

		graphicsEngine = new GraphicsEngine(windowTitle, graphicsConfig, scene, inputEventStream)
		graphicsEngine.on(WindowCreatedEvent) { event ->
			inputEventStream.addInputSource(graphicsEngine.graphicsContext)
		}
		graphicsEngine.on(EngineLoopStartEvent) { event ->
			applicationReady.countDown()
		}
		graphicsEngine.on(EngineStoppedEvent) { event ->
			stop()
		}

		gameClock = new GameClock()

		def gameClockTask = executorService.submit(gameClock)
		def audioEngineTask = executorService.submit(audioEngine)
		def graphicsEngineTask = executorService.submit(graphicsEngine)

		// Start the application
		logger.debug('Starting application...')
		applicationReady.await()
		applicationStart()

		graphicsEngineTask.get()
		audioEngineTask.get()
		gameClockTask.get()
	}

	/**
	 * Stop the application.
	 */
	final void stop() {

		applicationStoppingSemaphore.tryAcquireAndRelease { ->
			if (!applicationStopped) {
				logger.debug('Stopping application...')
				applicationStop()

				graphicsEngine.stop()
				audioEngine.stop()
				gameClock.stop()

				applicationStopped = true
			}
		}
	}
}
