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

package nz.net.ultraq.redhorizon

import nz.net.ultraq.redhorizon.engine.ContextErrorEvent
import nz.net.ultraq.redhorizon.engine.EngineStoppedEvent
import nz.net.ultraq.redhorizon.engine.GameClock
import nz.net.ultraq.redhorizon.engine.EngineLoopStopEvent
import nz.net.ultraq.redhorizon.engine.audio.AudioConfiguration
import nz.net.ultraq.redhorizon.engine.audio.AudioEngine
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsEngine
import nz.net.ultraq.redhorizon.engine.graphics.WindowCreatedEvent
import nz.net.ultraq.redhorizon.engine.graphics.imgui.GuiEvent
import nz.net.ultraq.redhorizon.engine.input.InputEventStream
import nz.net.ultraq.redhorizon.events.Event
import nz.net.ultraq.redhorizon.events.EventTarget
import nz.net.ultraq.redhorizon.scenegraph.Scene
import static nz.net.ultraq.redhorizon.engine.graphics.imgui.GuiEvent.*

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.transform.TupleConstructor
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.FutureTask

/**
 * A base for developing an application that uses the Red Horizon engine, this
 * class sets up the engine components and provides access to those and the
 * scene the engine was created to render.
 * 
 * @author Emanuel Rabina
 */
@TupleConstructor
abstract class Application implements EventTarget, Runnable {

	private static final Logger logger = LoggerFactory.getLogger(Application)

	final String windowTitle
	final AudioConfiguration audioConfig
	final GraphicsConfiguration graphicsConfig

	protected AudioEngine audioEngine
	protected GameClock gameClock
	protected GraphicsEngine graphicsEngine
	protected InputEventStream inputEventStream
	protected Scene scene = new Scene()

	private final ExecutorService executorService = Executors.newCachedThreadPool()

	/**
	 * Begin the application.
	 */
	void start() {

		logger.debug('Starting application')

		// Start the engines
		FutureTask<?> executable
		def executionBarrier = new CyclicBarrier(2)
		def exception

		inputEventStream = new InputEventStream()
		inputEventStream.on(GuiEvent) { event ->
			if (event.type == EVENT_TYPE_STOP) {
				stop()
			}
		}

		audioEngine = new AudioEngine(audioConfig, scene)

		def graphicsEngineStopped = false
		graphicsEngine = new GraphicsEngine(windowTitle, graphicsConfig, scene, inputEventStream, { toExecute ->
			executable = toExecute
			executionBarrier.await()
		})
		graphicsEngine.on(ContextErrorEvent) { event ->
			exception = event.exception
			executionBarrier.await()
		}
		graphicsEngine.on(EngineLoopStopEvent) { event ->
			if (event.exception) {
				exception = event.exception
			}
			executionBarrier.await()
		}
		graphicsEngine.on(EngineStoppedEvent) { event ->
			graphicsEngineStopped = true
			executionBarrier.await()
		}
		graphicsEngine.on(WindowCreatedEvent) { event ->
			inputEventStream.addInputSource(graphicsEngine.graphicsContext)
		}

		gameClock = new GameClock()

		def gameClockTask = executorService.submit(gameClock)
		def audioEngineTask = executorService.submit(audioEngine)
		def graphicsEngineTask = executorService.submit(graphicsEngine)

		// Start the application
		executorService.submit(this)

		// Maintain a loop in this (main) thread for whenever the graphics engine
		// needs to execute something using it
		while (!graphicsEngineStopped) {
			executionBarrier.await()
			if (executable) {
				executionBarrier.reset()
				def executableRef = executable
				executable = null
				executableRef.run()
			}
		}

		logger.debug('Stopping application')
		stop()
		graphicsEngineTask.get()
		audioEngineTask.get()
		gameClockTask.get()
		logger.debug('Application stopped')
	}

	/**
	 * End the application.
	 */
	void stop() {

		trigger(new ApplicationStoppingEvent(), true)

		graphicsEngine.stop()
		audioEngine.stop()
		gameClock.stop()
	}

	/**
	 * Event for when the application is about to be stopped, before the engines
	 * are shut down.  Can be used for performing any cleanup tasks.
	 */
	class ApplicationStoppingEvent extends Event {}
}
