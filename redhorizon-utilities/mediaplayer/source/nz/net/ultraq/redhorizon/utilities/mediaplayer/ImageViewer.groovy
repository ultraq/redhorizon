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

package nz.net.ultraq.redhorizon.utilities.mediaplayer

import nz.net.ultraq.redhorizon.engine.KeyEvent
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.WindowCreatedEvent
import nz.net.ultraq.redhorizon.engine.graphics.WithGraphicsEngine
import nz.net.ultraq.redhorizon.filetypes.ImageFile
import nz.net.ultraq.redhorizon.media.Image

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE
import static org.lwjgl.glfw.GLFW.GLFW_PRESS

import groovy.transform.TupleConstructor
import java.util.concurrent.Executors

/**
 * A basic image viewer, used primarily for testing purposes.
 * 
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class ImageViewer implements WithGraphicsEngine {

	private static final Logger logger = LoggerFactory.getLogger(ImageViewer)

	final ImageFile imageFile

	final boolean filter
	final boolean fixAspectRatio

	/**
	 * View the configured file.
	 */
	void view() {

		logger.info('File details: {}', imageFile)

		def config = new GraphicsConfiguration(
			filter: filter,
			fixAspectRatio: fixAspectRatio
		)

		Executors.newCachedThreadPool().executeAndShutdown { executorService ->
			withGraphicsEngine(executorService, config) { graphicsEngine ->

				// Add the image to the engine once we have the window dimensions
				graphicsEngine.on(WindowCreatedEvent) { event ->
					graphicsEngine.addSceneElement(new Image(imageFile, calculateCenteredDimensions(
						imageFile.width, imageFile.height, event.viewportSize)
					))
				}

				logger.info('Displaying the image in another window.  Close the window to exit.')

				// Key event handler
				graphicsEngine.on(KeyEvent) { event ->
					if (event.action == GLFW_PRESS) {
						switch (event.key) {
						case GLFW_KEY_ESCAPE:
							graphicsEngine.stop()
							break
						}
					}
				}
			}
		}
	}
}
