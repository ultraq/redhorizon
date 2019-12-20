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

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.transform.TupleConstructor
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

	/**
	 * View the configured file.
	 */
	void view() {

		logger.info("File details: ${imageFile}")

		Executors.newCachedThreadPool().executeAndShutdown { executorService ->
			def image = new Image(imageFile)

			// To allow the graphics engine to submit items to execute in this thread
			FutureTask executable = null
			def graphicsEngine = new GraphicsEngine(image, { toExecute ->
				executable = toExecute
			})

			def engine = executorService.submit(graphicsEngine)

			logger.info('Displaying the image in another window.  Close the window to exit.')

			// Execute things from this thread when needed
			while (!engine.done) {
				while (!engine.done && !executable) {
					Thread.onSpinWait()
				}
				if (executable) {
					executable.run()
					executable = null
				}
			}

			engine.get()
		}
	}
}
