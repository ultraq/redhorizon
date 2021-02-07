/* 
 * Copyright 2021, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GLCapabilities
import static org.lwjgl.opengl.GL21.*
import static org.lwjgl.opengl.GL33C.*

import java.nio.ByteBuffer

/**
 * Common code between both of the OpenGL renderers.
 * 
 * @author Emanuel Rabina
 */
abstract class OpenGLRenderer implements GraphicsRenderer, AutoCloseable {

	protected final GLCapabilities capabilities
	protected final Colour clearColour
	protected final boolean filter

	/**
	 * Constructor, copies out the configuration.
	 * 
	 * @param config
	 */
	OpenGLRenderer(GraphicsConfiguration config) {

		capabilities = GL.createCapabilities()

		clearColour = config.clearColour
		filter = config.filter

		glClearColor(clearColour.r, clearColour.g, clearColour.b, 1)
	}

	/**
	 * Check for any OpenGL errors created by the OpenGL call in the given
	 * closure, throwing them if they occur.
	 * 
	 * @param closure
	 */
	protected static <T> T checkForError(Closure<T> closure) {

		def result = closure()
		def error = glGetError()
		if (error != GL_NO_ERROR) {
			def errorCode =
				error == GL_INVALID_ENUM ? 'GL_INVALID_ENUM' :
				error == GL_INVALID_VALUE ? 'GL_INVALID_VALUE' :
				error == GL_INVALID_OPERATION ? 'GL_INVALID_OPERATION' :
				error == GL_INVALID_FRAMEBUFFER_OPERATION ? 'GL_INVALID_FRAMEBUFFER_OPERATION' : // OpenGL 3.0+
				error == GL_OUT_OF_MEMORY ? 'GL_OUT_OF_MEMORY' :
				error == GL_STACK_UNDERFLOW ? 'GL_STACK_UNDERFLOW' :
				error == GL_STACK_OVERFLOW ? 'GL_STACK_OVERFLOW' :
				error
			throw new Exception("OpenGL error: ${errorCode}")
		}
		return result
	}

	@Override
	void clear() {

		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)
	}

	@Override
	Texture createTexture(ByteBuffer data, int format, int width, int height, boolean filter = this.filter) {

		int textureId = checkForError { ->
			return glGenTextures()
		}
		checkForError { ->
			glBindTexture(GL_TEXTURE_2D, textureId)
		}
		checkForError { ->
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filter ? GL_LINEAR : GL_NEAREST)
		}
		checkForError { ->
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filter ? GL_LINEAR : GL_NEAREST)
		}

		def colourFormat =
			format == 3 ? GL_RGB :
			format == 4 ? GL_RGBA :
			0
		checkForError { ->
			glTexImage2D(GL_TEXTURE_2D, 0, colourFormat, width, height, 0, colourFormat, GL_UNSIGNED_BYTE, ByteBuffer.fromBuffersDirect(data))
		}

		return new Texture(
			textureId: textureId
		)
	}

	@Override
	void deleteTexture(Texture texture) {

		checkForError { ->
			glDeleteTextures(texture.textureId)
		}
	}
}
