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
import nz.net.ultraq.redhorizon.engine.RenderLoopStopEvent
import nz.net.ultraq.redhorizon.engine.audio.AudioEngine
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsEngine
import nz.net.ultraq.redhorizon.geometry.Dimension

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import java.util.concurrent.CountDownLatch
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.ExecutorService
import java.util.concurrent.FutureTask

/**
 * An abstract class for developing an application, this class provides
 * closure-style helper methods to combine the Red Horizon engine components
 * into a program of any kind.
 * 
 * @author Emanuel Rabina
 */
abstract class Application {

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
	 * Execute the given closure within the context of having an audio engine;
	 * setting it up, passing it along to the closure, and finally shutting it
	 * down.
	 * 
	 * @param executorService
	 * @param closure
	 */
	protected static void useAudioEngine(ExecutorService executorService,
		@ClosureParams(value = SimpleType, options = 'nz.net.ultraq.redhorizon.engine.audio.AudioEngine')
		Closure closure) {

		def audioEngine = new AudioEngine()
		def engine = executorService.submit(audioEngine)

		closure(audioEngine)

		engine.get()
	}

	/**
	 * Execute the given closure within the context of having a game clock;
	 * setting it up, passing it to the closure, and shutting it down when the
	 * closure is complete.
	 * 
	 * @param executorService
	 * @param closure
	 */
	protected static void useGameClock(ExecutorService executorService,
		@ClosureParams(value = SimpleType, options = 'nz.net.ultraq.redhorizon.engine.GameClock')
		Closure closure) {

		def gameClock = new GameClock(executorService)

		closure(gameClock)

		gameClock.stop()
	}

	/**
	 * Execute the given closure within the context of having a graphics engine;
	 * setting it up, passing it along to the closure, and finally shutting it
	 * down.
	 * 
	 * @param executorService
	 * @param config
	 * @param closure
	 */
	protected static void useGraphicsEngine(ExecutorService executorService, GraphicsConfiguration config,
		@ClosureParams(value = SimpleType, options = 'nz.net.ultraq.redhorizon.engine.graphics.GraphicsEngine')
		Closure closure) {

		def executionBarrier = new CyclicBarrier(2)
		def finishBarrier = new CountDownLatch(1)
		def exception

		// To allow the graphics engine to submit items to execute in this thread
		FutureTask executable = null
		def graphicsEngine = new GraphicsEngine(config, { toExecute ->
			executable = toExecute
			executionBarrier.await()
		})
		graphicsEngine.on(ContextErrorEvent) { event ->
			finishBarrier.countDown()
			exception = event.exception
			executionBarrier.await()
		}
		graphicsEngine.on(RenderLoopStopEvent) { event ->
			finishBarrier.countDown()
			if (event.exception) {
				exception = event.exception
				executionBarrier.await()
			}
		}
		def engine = executorService.submit(graphicsEngine)

		closure(graphicsEngine)

		// Execute things from this thread when needed
		while (!engine.done) {
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

		engine.get()
	}
}
