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

import org.joml.Vector2f
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.ByteBuffer

/**
 * A resource for loading multi-image files into a single texture.
 *
 * TODO: There's enough overlap here with the Image class that I might be able
 *       to modify Image to include sprite sheet support.
 *
 * @author Emanuel Rabina
 */
class SpriteSheet implements AutoCloseable {

	private static final Logger logger = LoggerFactory.getLogger(SpriteSheet)
	private static final int MAX_TEXTURE_WIDTH = 1024

	private volatile List<ByteBuffer> imageData = []
	final int width
	final int height
	private final int format
	final int numFrames
	private final int framesX
	private final float frameWidth
	private final float frameHeight
	final Texture texture
	private final Vector2f framePosition = new Vector2f()

	/**
	 * Constructor, create a new sprite sheet using its name and a stream of data.
	 */
	SpriteSheet(String fileName, InputStream inputStream) {

		var result = ImageDecoders
			.forFileExtension(fileName.substring(fileName.lastIndexOf('.') + 1))
			.on(FrameDecodedEvent) { event ->
				imageData << event.data().flipVertical(event.width(), event.height(), event.format())
			}
			.decode(inputStream)
		while (imageData.size() != result.frames()) {
			Thread.onSpinWait()
		}

		var fileInformation = result.fileInformation()
		if (fileInformation) {
			logger.info('{}: {}', fileName, fileInformation)
		}

		width = result.width()
		height = result.height()
		format = result.format()
		numFrames = result.frames()
		framesX = MAX_TEXTURE_WIDTH / width as int

		var textureData = (imageData as ByteBuffer[]).combine(width, height, format, framesX)
		var textureWidth = width * framesX
		var textureHeight = height * Math.ceil(result.frames() / framesX) as int
		texture = new OpenGLTexture(textureWidth, textureHeight, format, textureData)

		frameWidth = width / textureWidth
		frameHeight = height / textureHeight
	}

	@Override
	void close() {

		texture.close()
	}

	/**
	 * Get the position in texture coordinates of the given frame in the sprite
	 * sheet.
	 */
	Vector2f getFramePosition(int index) {

		var x = (index % framesX) * frameWidth
		var y = ((index / framesX) as int) * frameHeight
		return framePosition.set(x, y)
	}
}
