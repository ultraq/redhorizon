/* 
 * Copyright 2013, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import org.joml.Rectanglef
import org.lwjgl.opengl.GL
import static org.lwjgl.opengl.GL11.*

import java.nio.ByteBuffer

/**
 * A graphics renderer using the OpenGL API.
 * 
 * @author Emanuel Rabina
 */
class OpenGLRenderer implements GraphicsRenderer {

	/**
	 * Constructor, creates an OpenGL renderer with a set of defaults for Red
	 * Horizon's 2D game engine.
	 * 
	 * @param context
	 */
	OpenGLRenderer(OpenGLContext context) {

		GL.createCapabilities()

		glClearColor(0, 0, 0, 1)

		// Edge smoothing
		glHint(GL_POINT_SMOOTH_HINT, GL_FASTEST)
		glHint(GL_LINE_SMOOTH_HINT, GL_FASTEST)
		glHint(GL_POLYGON_SMOOTH_HINT, GL_FASTEST)
		glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_FASTEST)

		// Disable antialiasing globally
//		if (gl.isExtensionAvailable("GL_ARB_multisample")) {
//			gl.glDisable(GL_MULTISAMPLE)
//		}

		// Texturing controls
		glEnable(GL_TEXTURE_2D)
//		gl.glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE)

		// Texture blend combo, create a mixture of GL_BLEND on RGB, GL_REPLACE on A
//		gl.glTexEnvi(GL_TEXTURE_ENV, GL_COMBINE_RGB, GL_INTERPOLATE)
//		gl.glTexEnvi(GL_TEXTURE_ENV, GL_COMBINE_ALPHA, GL_REPLACE)
//		gl.glTexEnvi(GL_TEXTURE_ENV, GL_SOURCE0_RGB, GL_PRIMARY_COLOR)
//		gl.glTexEnvi(GL_TEXTURE_ENV, GL_SOURCE0_ALPHA, GL_PRIMARY_COLOR)
//		gl.glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND0_RGB, GL_SRC_COLOR)
//		gl.glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND0_ALPHA, GL_SRC_ALPHA)
//		gl.glTexEnvi(GL_TEXTURE_ENV, GL_SOURCE1_RGB, GL_PRIMARY_COLOR)
//		gl.glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND1_RGB, GL_SRC_COLOR)
//		gl.glTexEnvi(GL_TEXTURE_ENV, GL_SOURCE2_RGB, GL_TEXTURE)
//		gl.glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND2_RGB, GL_SRC_COLOR)

		// Depth testing
		glEnable(GL_DEPTH_TEST)
		glDepthFunc(GL_LEQUAL)

		// Alpha testing
		glEnable(GL_ALPHA_TEST)
		glAlphaFunc(GL_GREATER, 0)

		// Blending and blending function
		glEnable(GL_BLEND)
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

		// Set up the viewport based on the camera settings
		def windowSize = context.windowSize
		glViewport(0, 0, windowSize.width, windowSize.height)
		glMatrixMode(GL_PROJECTION)
		glLoadIdentity()
		glOrtho(
			-windowSize.width / 2, windowSize.width / 2,
			-windowSize.height / 2, windowSize.height / 2,
			0, 100
		)
		glMatrixMode(GL_MODELVIEW)
		glLoadIdentity()
	}

//	/**
//	 * {@inheritDoc}
//	 */
//	@Override
//	void updateCamera(Camera camera) {
//
//		// Update the position of the camera
//		Point3D diff = getPosition().difference(lastpos)
//		if (!diff.equals(Point3D.DEFAULT)) {
//			gl.glTranslatef(-diff.getX(), -diff.getY(), -diff.getZ())
//			lastpos = lastpos.add(diff)
//		}
//	}

	/**
	 * Check for any OpenGL errors created by the OpenGL call in the given
	 * closure, throwing them if they occur.
	 * 
	 * @param closure
	 */
	private static <T> T checkForError(Closure<T> closure) {

		def result = closure()
		def error = glGetError()
		if (error != GL_NO_ERROR) {
			def errorCode =
				error == GL_INVALID_ENUM ? 'GL_INVALID_ENUM' :
				error == GL_INVALID_VALUE ? 'GL_INVALID_VALUE' :
				error == GL_INVALID_OPERATION ? 'GL_INVALID_OPERATION' :
//				error == GL_INVALID_FRAMEBUFFER_OPERATION ? 'GL_INVALID_FRAMEBUFFER_OPERATION' : // OpenGL 3.0+
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

		checkForError { -> glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT) }
//		checkForError { -> glClear(GL_STENCIL_BUFFER_BIT) }
	}

	@Override
	int createTexture(ByteBuffer data, int format, int width, int height, boolean filter) {

		int textureId = checkForError { ->
			return glGenTextures()
		}
		checkForError { ->
			glBindTexture(GL_TEXTURE_2D, textureId)
		}
		checkForError { -> glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP) }
		checkForError { -> glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP) }
		checkForError { -> glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filter ? GL_LINEAR : GL_NEAREST) }
		checkForError { -> glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filter ? GL_LINEAR : GL_NEAREST) }

		def colourFormat =
			format == 3 ? GL_RGB :
			format == 4 ? GL_RGBA :
			0
		checkForError { ->
			glTexImage2D(GL_TEXTURE_2D, 0, colourFormat, width, height, 0, colourFormat, GL_UNSIGNED_BYTE, data)
		}

		return textureId
	}

	@Override
	void deleteTextures(int... textureIds) {

		checkForError { ->
			glDeleteTextures(textureIds)
		}
	}

	@Override
	void drawTexture(int textureId, Rectanglef rectangle, boolean flipVertical = true) {

		checkForError { -> glBindTexture(GL_TEXTURE_2D, textureId) }
		checkForError { -> glColor4f(1, 1, 1, 1) }
		glBegin(GL_QUADS)
			glTexCoord2f(0, flipVertical ? 1 : 0); glVertex2f(rectangle.minX, rectangle.minY)
			glTexCoord2f(0, flipVertical ? 0 : 1); glVertex2f(rectangle.minX, rectangle.maxY)
			glTexCoord2f(1, flipVertical ? 0 : 1); glVertex2f(rectangle.maxX, rectangle.maxY)
			glTexCoord2f(1, flipVertical ? 1 : 0); glVertex2f(rectangle.maxX, rectangle.minY)
		checkForError { -> glEnd() }
	}
}
