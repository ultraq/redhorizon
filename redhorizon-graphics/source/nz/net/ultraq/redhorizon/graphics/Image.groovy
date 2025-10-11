/*
 * Copyright 2025, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.graphics

import nz.net.ultraq.redhorizon.graphics.ImageDecoder.FrameDecodedEvent
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLTexture

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.ByteBuffer

/**
 * A resource for loading image files into textures, for re-use in other
 * graphics objects.
 *
 * @author Emanuel Rabina
 */
class Image implements AutoCloseable {

	private static final Logger logger = LoggerFactory.getLogger(Image)

	final Texture texture
	final int width
	final int height

	/**
	 * Constructor, create a new image using its name and a stream of data.
	 */
	Image(String fileName, InputStream inputStream) {

		ByteBuffer imageData = null
		Palette palette = null
		var result = ImageDecoders
			.forFileExtension(fileName.substring(fileName.lastIndexOf('.') + 1))
			.on(FrameDecodedEvent) { event ->
				imageData = event.data().flipVertical(event.width(), event.height(), event.channels())
				palette = event.palette()
			}
			.decode(inputStream)
		while (!imageData) {
			Thread.onSpinWait()
		}

		var fileInformation = result.fileInformation()
		if (fileInformation) {
			logger.info('{}: {}', fileName, fileInformation)
		}

		width = result.width()
		height = result.height()

		texture = palette ?
			new OpenGLTexture(width, height, palette.channels, imageData.applyPalette(palette)) :
			new OpenGLTexture(width, height, result.channels(), imageData)
	}

	@Override
	void close() {

		texture?.close()
	}
}
