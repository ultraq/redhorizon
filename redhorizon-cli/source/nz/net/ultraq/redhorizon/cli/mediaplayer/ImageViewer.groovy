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

package nz.net.ultraq.redhorizon.cli.mediaplayer

import nz.net.ultraq.redhorizon.Application
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.WindowCreatedEvent
import nz.net.ultraq.redhorizon.engine.input.KeyEvent
import nz.net.ultraq.redhorizon.filetypes.ImageFile
import nz.net.ultraq.redhorizon.media.Image

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE
import static org.lwjgl.glfw.GLFW.GLFW_PRESS

/**
 * A basic image viewer, used primarily for testing purposes.
 * 
 * @author Emanuel Rabina
 */
class ImageViewer extends Application {

	private static final Logger logger = LoggerFactory.getLogger(ImageViewer)

	final ImageFile imageFile

	/**
	 * Constructor, set the image to be displayed.
	 * 
	 * @param graphicsConfig
	 * @param imageFile
	 */
	ImageViewer(GraphicsConfiguration graphicsConfig, ImageFile imageFile) {

		super(null, graphicsConfig)
		this.imageFile = imageFile
	}

	@Override
	void run() {

		logger.info('File details: {}', imageFile)

		// Add the image to the engine once we have the window dimensions
		graphicsEngine.on(WindowCreatedEvent) { event ->
			scene << new Image(imageFile)
				.scaleXY(calculateScaleForFullScreen(imageFile.width, imageFile.height, event.renderSize))
				.translate(-imageFile.width / 2, -imageFile.height / 2, 0)
		}

		logger.info('Displaying the image in another window.  Close the window to exit.')

		// Key event handler
		inputEventStream.on(KeyEvent) { event ->
			if (event.action == GLFW_PRESS) {
				switch (event.key) {
				case GLFW_KEY_ESCAPE:
					stop()
					break
				}
			}
		}
	}
}
