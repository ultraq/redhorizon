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

package nz.net.ultraq.redhorizon

import nz.net.ultraq.redhorizon.engine.graphics.GraphicsEngine
import nz.net.ultraq.redhorizon.filetypes.ImageFile
import nz.net.ultraq.redhorizon.media.Image

import org.joml.Rectanglef
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.transform.TupleConstructor
import java.util.concurrent.CountDownLatch
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.FutureTask

/**
 * A basic image viewer, used primarily for testing purposes.
 * 
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class ImageViewer {

	private static final Logger logger = LoggerFactory.getLogger(ImageViewer)

	final ImageFile imageFile
	final boolean fixAspectRatio

	/**
	 * View the configured file.
	 */
	void view() {

		logger.info("File details: ${imageFile}")

		Executors.newCachedThreadPool().executeAndShutdown { executorService ->
			def width = imageFile.width
			def height = fixAspectRatio ? imageFile.height * 1.2 : imageFile.height
			def image = new Image(imageFile, new Rectanglef(-width / 2, -height / 2, width / 2, height / 2))

			def executionBarrier = new CyclicBarrier(2)
			def finishBarrier = new CountDownLatch(1)

			// To allow the graphics engine to submit items to execute in this thread
			FutureTask executable = null
			def graphicsEngine = new GraphicsEngine(image, { toExecute ->
				executable = toExecute
				executionBarrier.await()
			})
			graphicsEngine.on(GraphicsEngine.EVENT_RENDER_LOOP_STOP) { event ->
				finishBarrier.countDown()
			}
			def engine = executorService.submit(graphicsEngine)

			logger.info('Displaying the image in another window.  Close the window to exit.')

			// Execute things from this thread when needed
			while (!engine.done) {
				executionBarrier.await()
				if (executable) {
					executable.run()
					executable = null
					executionBarrier.reset()
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
}
