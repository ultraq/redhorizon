/*
 * Copyright 2023, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine.graphics.opengl

import nz.net.ultraq.redhorizon.engine.graphics.Texture
import nz.net.ultraq.redhorizon.filetypes.ColourFormat
import static nz.net.ultraq.redhorizon.filetypes.ColourFormat.*

import static org.lwjgl.opengl.GL11C.*
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0
import static org.lwjgl.opengl.GL13C.glActiveTexture
import static org.lwjgl.system.MemoryStack.stackPush
import static org.lwjgl.system.MemoryUtil.NULL

import java.nio.ByteBuffer

/**
 * OpenGL-specific texture implementation.
 *
 * @author Emanuel Rabina
 */
class OpenGLTexture extends Texture {

	final int textureId

	/**
	 * Constructor, builds an empty OpenGL texture whose data will be filled in
	 * later.
	 *
	 * @param width
	 * @param height
	 * @param filter
	 */
	OpenGLTexture(int width, int height, boolean filter = false) {

		super(width, height)

		textureId = glGenTextures()
		glBindTexture(GL_TEXTURE_2D, textureId)
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filter ? GL_LINEAR : GL_NEAREST)
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filter ? GL_LINEAR : GL_NEAREST)
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, NULL)
	}

	/**
	 * Constructor, builds an OpenGL texture from an image bytes.
	 *
	 * @param width
	 * @param height
	 * @param format
	 * @param data
	 */
	OpenGLTexture(int width, int height, ColourFormat format, ByteBuffer data) {

		super(width, height)

		textureId = glGenTextures()
		glBindTexture(GL_TEXTURE_2D, textureId)
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)

		var colourFormat = switch (format) {
			case FORMAT_INDEXED -> GL_RED
			case FORMAT_RGB -> GL_RGB
			case FORMAT_RGBA -> GL_RGBA
			default -> 0
		}
		var textureBuffer = stackPush().withCloseable { stack ->
			return stack.malloc(data.remaining())
				.put(data.array(), data.position(), data.remaining())
				.flip()
		}
		var matchesAlignment = (width * format.value) % 4 == 0
		if (!matchesAlignment) {
			glPixelStorei(GL_UNPACK_ALIGNMENT, 1)
		}
		glTexImage2D(GL_TEXTURE_2D, 0, colourFormat, width, height, 0, colourFormat, GL_UNSIGNED_BYTE, textureBuffer)
		if (!matchesAlignment) {
			glPixelStorei(GL_UNPACK_ALIGNMENT, 4)
		}
	}

	/**
	 * A texture is also considered falsey if it has since been deleted.
	 */
	boolean asBoolean() {

		return glIsTexture(textureId)
	}


	@Override
	void bind(int textureUnit = -1) {

		if (textureUnit != -1) {
			glActiveTexture(GL_TEXTURE0 + textureUnit)
		}
		glBindTexture(GL_TEXTURE_2D, textureId)
	}

	@Override
	void close() {

		glDeleteTextures(textureId)
	}
}
