/*
 * Copyright 2020, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import nz.net.ultraq.redhorizon.Application
import nz.net.ultraq.redhorizon.classic.PaletteTypes
import nz.net.ultraq.redhorizon.classic.filetypes.pal.PalFile
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsElement
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.Texture
import nz.net.ultraq.redhorizon.engine.graphics.WindowCreatedEvent
import nz.net.ultraq.redhorizon.engine.input.KeyEvent
import nz.net.ultraq.redhorizon.filetypes.ImagesFile
import nz.net.ultraq.redhorizon.filetypes.Palette
import nz.net.ultraq.redhorizon.media.Image
import nz.net.ultraq.redhorizon.scenegraph.SceneElement
import nz.net.ultraq.redhorizon.scenegraph.SceneVisitor
import static nz.net.ultraq.redhorizon.filetypes.ColourFormat.FORMAT_INDEXED

import org.joml.Vector3f
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE
import static org.lwjgl.glfw.GLFW.GLFW_PRESS
import static org.lwjgl.glfw.GLFW.GLFW_REPEAT

import groovy.transform.TupleConstructor
import java.util.concurrent.Executors

/**
 * A basic image viewer for viewing a file that contains multiple images, used
 * primarily for testing purposes.
 * 
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class ImagesViewer extends Application {

	private static final Logger logger = LoggerFactory.getLogger(ImagesViewer)
	private static final int TICK = 48

	final ImagesFile imagesFile
	final GraphicsConfiguration graphicsConfig
	final PaletteTypes paletteType

	/**
	 * View the configured file.
	 */
	void view() {

		logger.info('File details: {}', imagesFile)

		Palette palette
		if (imagesFile.format == FORMAT_INDEXED) {
			palette = getResourceAsStream(paletteType.file).withBufferedStream { inputStream ->
				return new PalFile(inputStream)
			}
		}

		Executors.newCachedThreadPool().executeAndShutdown { executorService ->
			useGraphicsEngine(executorService, graphicsConfig) { graphicsEngine ->

				def center = new Vector3f()

				// Represent each frame of the image in a long strip
				graphicsEngine.on(WindowCreatedEvent) { event ->
					graphicsEngine.scene << new ImageStrip(imagesFile, palette)
				}

				logger.info('Displaying the image in another window.  Close the window to exit.')

				// Key event handler
				graphicsEngine.on(KeyEvent) { event ->
					if (event.action == GLFW_PRESS || event.action == GLFW_REPEAT) {
						switch (event.key) {
						// Add options so it's not hard-coded to my weird inverted setup 😅
						case GLFW_KEY_LEFT:
							graphicsEngine.camera.translate(TICK, 0)
							break
						case GLFW_KEY_RIGHT:
							graphicsEngine.camera.translate(-TICK, 0)
							break
						case GLFW_KEY_SPACE:
							graphicsEngine.camera.center(center)
							break
						case GLFW_KEY_ESCAPE:
							graphicsEngine.stop()
							break
						}
					}
				}
			}
		}
	}

	/**
	 * A series of images laid out in a long horizontal strip.
	 */
	private class ImageStrip implements SceneElement, GraphicsElement {

		final List<Image> images = []
		final Palette palette
		private Texture paletteTexture

		/**
		 * Constructor, build a strip of images from a file containing multiple
		 * images.
		 * 
		 * @param imagesFile
		 * @param palette
		 */
		ImageStrip(ImagesFile imagesFile, Palette palette) {

			this.palette = palette
			this.bounds.set(0, 0, imagesFile.width * imagesFile.numImages, imagesFile.height)

			imagesFile.numImages.times { i ->
				def image = new Image(imagesFile, i)
				image.translate(new Vector3f(imagesFile.width * i, -imagesFile.height / 2, 0))
				images << image
			}
		}

		@Override
		void accept(SceneVisitor visitor) {

			visitor.visit(this)
			images.each { image ->
				visitor.visit(image)
			}
		}

		@Override
		void delete(GraphicsRenderer renderer) {

			renderer.deleteTexture(paletteTexture)
		}

		@Override
		void init(GraphicsRenderer renderer) {

			paletteTexture = renderer.createTexturePalette(palette)
		}

		@Override
		void render(GraphicsRenderer renderer) {

			renderer.setPalette(paletteTexture)
		}
	}
}
