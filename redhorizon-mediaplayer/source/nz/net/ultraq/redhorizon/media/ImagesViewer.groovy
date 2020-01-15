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

package nz.net.ultraq.redhorizon.media

import nz.net.ultraq.redhorizon.engine.graphics.WindowCreatedEvent
import nz.net.ultraq.redhorizon.filetypes.ImagesFile
import nz.net.ultraq.redhorizon.filetypes.Palette
import nz.net.ultraq.redhorizon.filetypes.pal.PalFile
import static nz.net.ultraq.redhorizon.filetypes.ColourFormat.FORMAT_INDEXED

import org.joml.Rectanglef
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.transform.TupleConstructor
import java.util.concurrent.Executors

/**
 * A basic image viewer for viewing a file that contains multiple images, used
 * primarily for testing purposes.
 * 
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class ImagesViewer implements Visual {

	private static final Logger logger = LoggerFactory.getLogger(ImagesViewer)

	final ImagesFile imagesFile
	final boolean filtering
	final boolean fixAspectRatio
	final String paletteType

	/**
	 * View the configured file.
	 */
	void view() {

		logger.info('File details: {}', imagesFile)

		Palette palette
		if (imagesFile.format == FORMAT_INDEXED) {
			def paletteName = "${paletteType.toLowerCase()}-temperat.pal"
			new BufferedInputStream(this.class.classLoader.getResourceAsStream(paletteName)).withCloseable { inputStream ->
				palette = new PalFile(inputStream)
			}
		}

		Executors.newCachedThreadPool().executeAndShutdown { executorService ->
			withGraphicsEngine(executorService, fixAspectRatio) { graphicsEngine ->

				// Build a combined image of all the images once we have the window size
				graphicsEngine.on(WindowCreatedEvent) { event ->
					def imagesAcross = imagesFile.imagesData.imagesAcross(imagesFile.width, event.windowSize.width)
					def combinedWidth = imagesFile.width * imagesAcross
					def combinedHeight = imagesFile.height * Math.ceil(imagesFile.numImages / imagesAcross) as int
					def combinedImage = imagesFile.imagesData.combineImages(imagesFile.width, imagesFile.height, imagesAcross)
					if (imagesFile.format == FORMAT_INDEXED) {
						combinedImage = combinedImage.applyPalette(palette)
					}
					graphicsEngine.addSceneElement(new Image(combinedWidth, combinedHeight,
						(palette?.format ?: imagesFile.format).value, combinedImage,
						centerDimensions(new Rectanglef(0, 0, combinedWidth, combinedHeight)),
						filtering
					))
				}

				logger.info('Displaying the image in another window.  Close the window to exit.')
			}
		}
	}
}