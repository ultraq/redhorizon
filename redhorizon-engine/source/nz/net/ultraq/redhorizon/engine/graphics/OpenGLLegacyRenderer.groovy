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

import org.joml.Matrix4f
import org.joml.Rectanglef
import org.joml.Vector2f
import org.joml.Vector3f
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.opengl.GL21.*

import java.nio.ByteBuffer
import java.nio.FloatBuffer

/**
 * A graphics renderer using legacy OpenGL APIs, so OpenGL 2.1 and older, and
 * fixed-function methods.
 * 
 * @author Emanuel Rabina
 */
class OpenGLLegacyRenderer extends OpenGLRenderer {

	private static final Logger logger = LoggerFactory.getLogger(OpenGLLegacyRenderer)

	private final Vector3f currentPosition = new Vector3f()

	/**
	 * Constructor, creates an OpenGL renderer with a set of defaults for Red
	 * Horizon's 2D game engine.
	 * 
	 * @param context
	 * @param config
	 */
	OpenGLLegacyRenderer(OpenGLContext context, GraphicsConfiguration config) {

		super(config)

		// Edge smoothing
		glHint(GL_POINT_SMOOTH_HINT, GL_FASTEST)
		glHint(GL_LINE_SMOOTH_HINT, GL_FASTEST)
		glHint(GL_POLYGON_SMOOTH_HINT, GL_FASTEST)
		glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_FASTEST)
		glLineWidth(2)

		// Texturing controls
		glEnable(GL_TEXTURE_2D)
		glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE)

		// Texture blend combo, create a mixture of GL_BLEND on RGB, GL_REPLACE on A
		glTexEnvi(GL_TEXTURE_ENV, GL_COMBINE_RGB, GL_INTERPOLATE)
		glTexEnvi(GL_TEXTURE_ENV, GL_COMBINE_ALPHA, GL_REPLACE)
		glTexEnvi(GL_TEXTURE_ENV, GL_SOURCE0_RGB, GL_PRIMARY_COLOR)
		glTexEnvi(GL_TEXTURE_ENV, GL_SOURCE0_ALPHA, GL_PRIMARY_COLOR)
		glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND0_RGB, GL_SRC_COLOR)
		glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND0_ALPHA, GL_SRC_ALPHA)
		glTexEnvi(GL_TEXTURE_ENV, GL_SOURCE1_RGB, GL_PRIMARY_COLOR)
		glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND1_RGB, GL_SRC_COLOR)
		glTexEnvi(GL_TEXTURE_ENV, GL_SOURCE2_RGB, GL_TEXTURE)
		glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND2_RGB, GL_SRC_COLOR)

		// Depth testing
		glEnable(GL_DEPTH_TEST)
		glDepthFunc(GL_LEQUAL)

		// Alpha testing
		glEnable(GL_ALPHA_TEST)
		glAlphaFunc(GL_GREATER, 0)

		// Blending and blending function
		glEnable(GL_BLEND)
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

		// Set up the viewport and projection
		def viewportSize = context.framebufferSize
		logger.debug('Establishing a viewport of size {}', viewportSize)
		glViewport(0, 0, viewportSize.width, viewportSize.height)
//		context.on(FramebufferSizeEvent) { event ->
//			logger.debug('Updating viewport to size {}x{}', event.width, event.height)
//			glViewport(0, 0, event.width, event.height)
//		}
	}

	@Override
	void close() {
	}

	@Override
	void createCamera(Matrix4f projection) {

		glMatrixMode(GL_PROJECTION)
		def projectionBuffer = FloatBuffer.allocateDirectNative(Matrix4f.FLOATS)
		projection.get(projectionBuffer)
		glLoadMatrixf(projectionBuffer)

		glMatrixMode(GL_MODELVIEW)
		glLoadIdentity()
	}

	@Override
	Lines createLines(Colour colour, Vector2f... vertices) {

		return new Lines(
			colour: colour,
			vertices: vertices
		)
	}

	@Override
	Texture createTexture(ByteBuffer data, int format, int width, int height, boolean filter = this.filter) {

		int textureId = checkForError { ->
			return glGenTextures()
		}
		checkForError { ->
			glBindTexture(GL_TEXTURE_2D, textureId)
		}
		checkForError { -> glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filter ? GL_LINEAR : GL_NEAREST) }
		checkForError { -> glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filter ? GL_LINEAR : GL_NEAREST) }

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
	void deleteLines(Lines lines) {
	}

	@Override
	void deleteTexture(MappedTexture texture) {

		def textureInstances = texture.parentTexture.instances
		textureInstances.remove(texture)
		if (textureInstances.empty) {
			checkForError { ->
				glDeleteTextures(texture.textureId)
			}
		}
	}

	@Override
	void drawLineLoop(Colour colour, Vector2f... vertices) {

		drawPrimitive(GL_LINE_LOOP, colour, vertices)
	}

	@Override
	void drawLines(Lines lines) {

		drawPrimitive(GL_LINES, lines.colour, lines.vertices)
	}

	/**
	 * Draw any kind of coloured primitive.
	 * 
	 * @param primitiveType
	 * @param colour
	 * @param vertices
	 */
	private static void drawPrimitive(int primitiveType, Colour colour, Vector2f... vertices) {

		withTextureEnvironmentMode(GL_COMBINE) { ->
			checkForError { -> glColor4f(colour.r, colour.g, colour.b, colour.a) }
			glBegin(primitiveType)
			vertices.each { vertex ->
				glVertex2f(vertex.x, vertex.y)
			}
			checkForError { -> glEnd() }
		}
	}

	@Override
	void drawTexture(MappedTexture texture) {

		def surface = texture.surface
		def repeatX = texture.repeatX
		def repeatY = texture.repeatY
		def flipVertical = texture.flipVertical

		checkForError { -> glBindTexture(GL_TEXTURE_2D, texture.textureId) }
		checkForError { -> glColor3f(1, 1, 1) }
		glBegin(GL_QUADS)
			glTexCoord2f(0, flipVertical ? repeatY : 0);       glVertex2f(surface.minX, surface.minY)
			glTexCoord2f(0, flipVertical ? 0 : repeatY);       glVertex2f(surface.minX, surface.maxY)
			glTexCoord2f(repeatX, flipVertical ? 0 : repeatY); glVertex2f(surface.maxX, surface.maxY)
			glTexCoord2f(repeatX, flipVertical ? repeatY : 0); glVertex2f(surface.maxX, surface.minY)
		checkForError { -> glEnd() }
	}

	@Override
	MappedTexture mapTexture(Texture texture, Rectanglef surface, float repeatX = 1, float repeatY = 1, boolean flipVertical = true) {

		// TODO: Move this to be handled by the Texture class?
		def mappedTexture = new MappedTexture(
			parentTexture: texture,
			surface: surface,
			repeatX: repeatX,
			repeatY: repeatY,
			flipVertical: flipVertical
		)
		texture.instances << mappedTexture
		return mappedTexture
	}

	/**
	 * Return some information about the renderer.
	 * 
	 * @return
	 */
	@Override
	String toString() {

		return """
			OpenGL graphics renderer
			 - Vendor: ${glGetString(GL_VENDOR)}
			 - Device name: ${glGetString(GL_RENDERER)}
			 - OpenGL version: ${glGetString(GL_VERSION)}
		""".stripIndent()
	}

	@Override
	void updateCamera(Vector3f position) {

		if (currentPosition.x != position.x || currentPosition.y != position.y) {
			glTranslatef(currentPosition.x - position.x as float, currentPosition.y - position.y as float, 0)
			currentPosition.set(position)
		}
	}

	/**
	 * Execute the given closure with its own texture environment mode independent
	 * from the global one.
	 * 
	 * @param texEnvMode
	 * @param closure
	 */
	private static void withTextureEnvironmentMode(int texEnvMode, Closure closure) {

		def origTexEnvMode = glGetTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE)
		glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, texEnvMode)
		closure()
		glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, origTexEnvMode)
	}
}
