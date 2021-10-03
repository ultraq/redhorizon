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
import nz.net.ultraq.redhorizon.engine.GameClock
import nz.net.ultraq.redhorizon.engine.EngineLoopStopEvent
import nz.net.ultraq.redhorizon.engine.audio.AudioConfiguration
import nz.net.ultraq.redhorizon.engine.audio.AudioEngine
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsEngine
import nz.net.ultraq.redhorizon.engine.input.InputEventStream
import nz.net.ultraq.redhorizon.geometry.Dimension
import nz.net.ultraq.redhorizon.scenegraph.Scene

import groovy.transform.TupleConstructor
import java.util.concurrent.CountDownLatch
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
abstract class Application implements Runnable {

	final AudioConfiguration audioConfig
	final GraphicsConfiguration graphicsConfig

	protected AudioEngine audioEngine
	protected GameClock gameClock
	protected GraphicsEngine graphicsEngine
	protected InputEventStream inputEventStream
	protected Scene scene = new Scene()

	private final ExecutorService executorService = Executors.newCachedThreadPool()

	/**
	 * Calculate how much to scale an image by to fit the full screen.
	 * 
	 * @param imageWidth
	 * @param imageHeight
	 * @param screen
	 * @return A factor of how much to scale the image by.
	 */
	protected static float calculateScaleForFullScreen(int imageWidth, int imageHeight, Dimension screen) {

		return Math.min(screen.width / imageWidth, screen.height / imageHeight)
	}

	/**
	 * Begin the application.
	 */
	void start() {

		// Start the engines
		FutureTask<?> executable
		def executionBarrier = new CyclicBarrier(2)
		def finishBarrier = new CountDownLatch(1)
		def exception

		audioEngine = new AudioEngine(audioConfig, scene)
		graphicsEngine = new GraphicsEngine(graphicsConfig, scene, { toExecute ->
			executable = toExecute
			executionBarrier.await()
		})
		graphicsEngine.on(ContextErrorEvent) { event ->
			finishBarrier.countDown()
			exception = event.exception
			executionBarrier.await()
		}
		graphicsEngine.on(EngineLoopStopEvent) { event ->
			finishBarrier.countDown()
			if (event.exception) {
				exception = event.exception
				executionBarrier.await()
			}
		}
		inputEventStream = new InputEventStream(graphicsEngine)
		gameClock = new GameClock()

		executorService.submit(gameClock)
		def audioEngineTask = executorService.submit(audioEngine)
		def graphicsEngineTask = executorService.submit(graphicsEngine)

		// Start the application
		executorService.submit(this)

		// For the graphics engine that needs to execute things from the main thread
		while (!graphicsEngineTask.done) {
			executionBarrier.await()
			if (executable) {
				executionBarrier.reset()
				def executableRef = executable
				executable = null
				executableRef.run()
			}

			// Shutdown phase
			if (exception || (graphicsEngine.started && graphicsEngine.stopped)) {
				finishBarrier.await()
				break
			}
		}

		stop()
		graphicsEngineTask.get()
		audioEngineTask.get()
	}

	/**
	 * End the application.
	 */
	void stop() {

		graphicsEngine.stop()
		audioEngine.stop()
		gameClock.stop()
	}
}
