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
import nz.net.ultraq.redhorizon.engine.audio.AudioSystem
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsSystem
import nz.net.ultraq.redhorizon.engine.graphics.WindowCreatedEvent
import nz.net.ultraq.redhorizon.engine.graphics.imgui.GuiEvent
import nz.net.ultraq.redhorizon.engine.input.InputEventStream
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.engine.time.GameClock
import static nz.net.ultraq.redhorizon.engine.graphics.imgui.GuiEvent.EVENT_TYPE_STOP

import org.slf4j.Logger
import org.slf4j.LoggerFactory

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

	protected Engine engine
	protected AudioSystem audioSystem
	protected GameClock gameClock
	protected GraphicsSystem graphicsSystem
	protected InputEventStream inputEventStream
	protected Scene scene = new Scene()
	protected boolean applicationStopped

	private final String windowTitle
	private final AudioConfiguration audioConfig
	private final GraphicsConfiguration graphicsConfig
	private final Semaphore applicationStoppingSemaphore = new Semaphore(1)

	/**
	 * Constructor, creates an application with the title suffix and
	 * configuration.
	 *
	 * @param windowTitle
	 * @param audioConfig
	 * @param graphicsConfig
	 */
	Application(String windowTitle, AudioConfiguration audioConfig = new AudioConfiguration(),
		GraphicsConfiguration graphicsConfig = new GraphicsConfiguration()) {

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

		// Create the necessary systems 
		engine = new Engine()

		inputEventStream = new InputEventStream()
		inputEventStream.on(GuiEvent) { event ->
			if (event.type == EVENT_TYPE_STOP) {
				stop()
			}
		}

		audioSystem = new AudioSystem(scene, audioConfig)
		engine << audioSystem

		graphicsSystem = new GraphicsSystem(scene, windowTitle, inputEventStream, graphicsConfig)
		graphicsSystem.on(WindowCreatedEvent) { event ->
			inputEventStream.addInputSource(event.window)
		}
		engine << graphicsSystem

		gameClock = new GameClock(scene)
		engine << gameClock

		// Start the application
		logger.debug('Starting application...')
		engine.start()
		applicationStart()

		engine.waitUntilStopped()
	}

	/**
	 * Stop the application.
	 */
	final void stop() {

		applicationStoppingSemaphore.tryAcquireAndRelease { ->
			if (!applicationStopped) {
				logger.debug('Stopping application...')
				applicationStop()
				engine.stop()
				applicationStopped = true
			}
		}
	}
}
