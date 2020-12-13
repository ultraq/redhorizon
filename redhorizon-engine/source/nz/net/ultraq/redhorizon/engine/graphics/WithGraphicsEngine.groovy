/* 
 * Copyright 2019, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine.graphics

import nz.net.ultraq.redhorizon.engine.RenderLoopStopEvent
import nz.net.ultraq.redhorizon.geometry.Dimension

import org.joml.Rectanglef

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import java.util.concurrent.CountDownLatch
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.ExecutorService
import java.util.concurrent.FutureTask

/**
 * Trait for simplifying the use of a graphics engine by keeping it to within a
 * closure.
 * 
 * @author Emanuel Rabina
 */
trait WithGraphicsEngine {

	/**
	 * Calculate the ideal centered dimensions for the image that stretches to fit
	 * the window while maintaining the target aspect ratio.
	 * 
	 * @param imageWidth
	 * @param imageHeight
	 * @param fixAspectRatio
	 * @param window
	 * @return
	 */
	Rectanglef calculateCenteredDimensions(int imageWidth, int imageHeight, boolean fixAspectRatio, Dimension window) {

		def width = window.width
		def height = imageHeight * (width / imageWidth)
		if (fixAspectRatio) {
			height *= 1.2
		}
		return centerDimensions(new Rectanglef(0, 0, width, height))
	}

	/**
	 * Center a set of dimensions.
	 * 
	 * @param dimensions
	 * @return The same dimensions, but modified to be centered around the origin.
	 */
	Rectanglef centerDimensions(Rectanglef dimensions) {

		return dimensions.translate(-dimensions.maxX / 2 as int, -dimensions.maxY / 2 as int)
	}

	/**
	 * Execute the given context within the context of having a graphics engine,
	 * setting it up, passing it along to the closure, and finally shutting it
	 * down.
	 * 
	 * @param executorService
	 * @param fixAspectRatio
	 * @param closure
	 */
	void withGraphicsEngine(ExecutorService executorService, boolean fixAspectRatio,
		@ClosureParams(value = SimpleType, options = 'nz.net.ultraq.redhorizon.engine.graphics.GraphicsEngine')
		Closure closure) {

		def executionBarrier = new CyclicBarrier(2)
		def finishBarrier = new CountDownLatch(1)

		// To allow the graphics engine to submit items to execute in this thread
		FutureTask executable = null
		def graphicsEngine = new GraphicsEngine(fixAspectRatio, { toExecute ->
			executable = toExecute
			executionBarrier.await()
		})
		graphicsEngine.on(RenderLoopStopEvent) { event ->
			finishBarrier.countDown()
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
			if (graphicsEngine.started && graphicsEngine.stopped) {
				finishBarrier.await()
				break
			}
		}

		engine.get()
	}
}