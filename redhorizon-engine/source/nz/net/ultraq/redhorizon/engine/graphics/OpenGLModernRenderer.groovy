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

import org.joml.Rectanglef
import org.joml.Vector2f
import org.joml.Vector3f
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.opengl.GL33C.*

import java.nio.ByteBuffer
import java.nio.FloatBuffer

/**
 * A graphics renderer using the OpenGL core profile, so OpenGL 3.3+ and shaders
 * only.
 * 
 * @author Emanuel Rabina
 */
class OpenGLModernRenderer extends OpenGLRenderer {

	private static final Logger logger = LoggerFactory.getLogger(OpenGLModernRenderer)

	private int linesShaderProgramId
	private int textureShaderProgramId

	/**
	 * Constructor, create a modern OpenGL renderer with a set of defaults for Red
	 * Horizon's 2D game engine.
	 * 
	 * @param context
	 * @param config
	 */
	OpenGLModernRenderer(OpenGLContext context, GraphicsConfiguration config) {

		super(config)

//		if (capabilities.GL_KHR_debug) {
//			glEnable(GL_DEBUG_OUTPUT)
//			glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS)
//			glDebugMessageCallback(new GLDebugMessageCallback() {
//				@Override
//				void invoke(int source, int type, int id, int severity, int length, long message, long userParam) {
//					logger.error('OpenGL error: {}', getMessage(length, message))
//				}
//			}, 0)
//		}

		// Edge smoothing
//		glHint(GL_POINT_SMOOTH_HINT, GL_FASTEST)
//		glHint(GL_LINE_SMOOTH_HINT, GL_FASTEST)
//		glHint(GL_POLYGON_SMOOTH_HINT, GL_FASTEST)
//		glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_FASTEST)
//		glLineWidth(2)

		// Texturing controls
//		glEnable(GL_TEXTURE_2D)

		// Depth testing
		checkForError { -> glEnable(GL_DEPTH_TEST) }
		checkForError { -> glDepthFunc(GL_LEQUAL) }

		// Blending and blending function
		checkForError { -> glEnable(GL_BLEND) }
		checkForError { -> glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA) }

		// Set up the viewport and projection
		def viewportSize = context.framebufferSize
		logger.debug('Establishing a viewport of size {}', viewportSize)
		checkForError { -> glViewport(0, 0, viewportSize.width, viewportSize.height) }
//		context.on(FramebufferSizeEvent) { event ->
//			logger.debug('Updating viewport to size {}x{}', event.width, event.height)
//			glViewport(0, 0, event.width, event.height)
//		}
	}

	void useProgram(int programId) {

		glUseProgram(programId)

		// TODO: 🤔
//		def colourAttrib = glGetAttribLocation(programId, 'colour')
//		glEnableVertexAttribArray(colourAttrib)
//		glVertexAttribPointer(colourAttrib, 4, GL_FLOAT, false, 6 * Float.BYTES, 0)
//
//		def positionAttrib = glGetAttribLocation(programId, 'position')
//		glEnableVertexAttribArray(positionAttrib)
//		glVertexAttribPointer(positionAttrib, 2, GL_FLOAT, false, 6 * Float.BYTES, 4 * Float.BYTES) // 4 bytes for RGBA and 2 for Vector2f
//
//		def uniformModel = glGetUniformLocation(programId, 'model')
//		def model = new Matrix4f()
////		glUniformMatrix4fv(uniformModel, false, model.get(FloatBuffer.allocateDirectNative(Matrix4f.BYTES)))
//		glUniformMatrix4fv(uniformModel, false, model as float[])
//
//		def viewModel = glGetUniformLocation(programId, 'view')
//		def view = new Matrix4f()
//		glUniformMatrix4fv(viewModel, false, view.get(FloatBuffer.allocateDirectNative(Matrix4f.BYTES)))
//
//		def projectionModel = glGetUniformLocation(programId, 'projection')
//		glUniformMatrix4fv(projectionModel, false, projection.get(FloatBuffer.allocateDirectNative(Matrix4f.BYTES)))
	}

	private void buildLinesShader() {

		def vertexShaderId = getResourceAsStream('nz/net/ultraq/redhorizon/engine/graphics/Default.vert').withBufferedStream { stream ->
			return checkShaderCompilation { ->
				return createShader(GL_VERTEX_SHADER, stream.text)
			}
		}
		def fragmentShaderId = getResourceAsStream('nz/net/ultraq/redhorizon/engine/graphics/Default.frag').withBufferedStream { stream ->
			return checkShaderCompilation { ->
				return createShader(GL_FRAGMENT_SHADER, stream.text)
			}
		}
		linesShaderProgramId = checkProgramLink { ->
			def programId = checkForError { -> glCreateProgram() }
			checkForError { -> glAttachShader(programId, vertexShaderId) }
			checkForError { -> glAttachShader(programId, fragmentShaderId) }
			checkForError { -> glLinkProgram(programId) }
			checkForError { -> glValidateProgram(programId) }
			return programId
		}
		checkForError { -> glDeleteShader(vertexShaderId) }
		checkForError { -> glDeleteShader(fragmentShaderId) }
		checkForError { -> glBindFragDataLocation(linesShaderProgramId, 0, 'vertexColour') }
	}

	private void buildTextureShader() {

		def vertexShaderId = getResourceAsStream('nz/net/ultraq/redhorizon/engine/graphics/Texture.vert').withBufferedStream { stream ->
			return checkShaderCompilation { ->
				return createShader(GL_VERTEX_SHADER, stream.text)
			}
		}
		def fragmentShaderId = getResourceAsStream('nz/net/ultraq/redhorizon/engine/graphics/Texture.frag').withBufferedStream { stream ->
			return checkShaderCompilation { ->
				return createShader(GL_FRAGMENT_SHADER, stream.text)
			}
		}
		textureShaderProgramId = checkProgramLink { ->
			def programId = checkForError { -> glCreateProgram() }
			checkForError { -> glAttachShader(programId, vertexShaderId) }
			checkForError { -> glAttachShader(programId, fragmentShaderId) }
			checkForError { -> glLinkProgram(programId) }
			checkForError { -> glValidateProgram(programId) }
			return programId
		}
		checkForError { -> glDeleteShader(vertexShaderId) }
		checkForError { -> glDeleteShader(fragmentShaderId) }
	}

	/**
	 * Execure a closure that links a program, checking the status upon completion
	 * and returning the program ID if successful, or throwing an exception if an
	 * error occurred.
	 * 
	 * @param closure
	 * @return
	 */
	private static int checkProgramLink(Closure<Integer> closure) {

		def programId = closure()
		def status = glGetProgrami(programId, GL_LINK_STATUS)
		if (status != GL_TRUE) {
			throw new RuntimeException(glGetProgramInfoLog(programId))
		}
		return programId
	}

	/**
	 * Execute a closure that creates a shader, checking the status upon
	 * completion and returning the shader ID if successful, or throwing an
	 * exception if an error occurred.
	 * 
	 * @param closure
	 * @return
	 */
	private static int checkShaderCompilation(Closure<Integer> closure) {

		def shaderId = closure()
		def status = glGetShaderi(shaderId, GL_COMPILE_STATUS)
		if (status != GL_TRUE) {
			throw new RuntimeException(glGetShaderInfoLog(shaderId))
		}
		return shaderId
	}

	@Override
	void close() {

		if (linesShaderProgramId) {
			glDeleteProgram(linesShaderProgramId)
		}
		if (textureShaderProgramId) {
			glDeleteProgram(textureShaderProgramId)
		}
	}

	@Override
	void createCamera(Rectanglef projection) {
	}

	@Override
	Lines createLines(Colour colour, Vector2f... vertices) {

		if (!linesShaderProgramId) {
			buildLinesShader()
		}

		def vertexArrayId = checkForError { -> glGenVertexArrays() }
		checkForError { -> glBindVertexArray(vertexArrayId) }

		def floatsPerVertex = Colour.FLOATS + Vector2f.FLOATS
		def bytesPerVertex = Colour.BYTES + Vector2f.BYTES

		def verticesBuffer = FloatBuffer.allocateDirectNative(floatsPerVertex * vertices.length)
		vertices.each { vertex ->
			verticesBuffer.put(colour as float[])
			verticesBuffer.put(vertex as float[])
		}
		verticesBuffer.flip()

		def bufferId = glGenBuffers()
		checkForError { -> glBindBuffer(GL_ARRAY_BUFFER, bufferId) }
		checkForError { -> glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW) }

		def colourAttrib = checkForError { -> glGetAttribLocation(linesShaderProgramId, 'colour') }
		checkForError { -> glEnableVertexAttribArray(colourAttrib) }
		checkForError { -> glVertexAttribPointer(colourAttrib, 4, GL_FLOAT, false, bytesPerVertex, 0) }
		def positionAttrib = checkForError { -> glGetAttribLocation(linesShaderProgramId, 'position') }
		checkForError { -> glEnableVertexAttribArray(positionAttrib) }
		checkForError { -> glVertexAttribPointer(positionAttrib, 2, GL_FLOAT, false, bytesPerVertex, Colour.BYTES) }

		return new Lines(
			vertexArrayId: vertexArrayId,
			bufferId: bufferId,
			colour: colour,
			vertices: vertices
		)
	}

	/**
	 * Create a shader of the specified type, running a compilation check to make
	 * sure it all went OK.
	 *
	 * @param shaderType
	 * @param shaderSource
	 * @return
	 */
	private static int createShader(int shaderType, String shaderSource) {

		return checkShaderCompilation { ->
			def shaderId = glCreateShader(shaderType)
			checkForError { -> glShaderSource(shaderId, shaderSource) }
			checkForError { -> glCompileShader(shaderId) }
			return shaderId
		}
	}

	@Override
	Texture createTexture(ByteBuffer data, int format, int width, int height, boolean filter = this.filter) {

		if (!textureShaderProgramId) {
			buildTextureShader()
		}

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

		checkForError { -> glDeleteBuffers(lines.bufferId) }
		checkForError { -> glDeleteVertexArrays(lines.vertexArrayId) }
	}

	@Override
	void deleteTexture(MappedTexture texture) {

		checkForError { -> glDeleteBuffers(texture.bufferId, texture.elementBufferId) }
		checkForError { -> glDeleteVertexArrays(texture.vertexArrayId) }

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
	}

	@Override
	void drawLines(Lines lines) {

		checkForError { -> glUseProgram(linesShaderProgramId) }
		checkForError { -> glBindVertexArray(lines.vertexArrayId) }
		checkForError { -> glDrawArrays(GL_LINES, 0, lines.vertices.length) }
	}

	@Override
	void drawTexture(MappedTexture texture) {

		checkForError { -> glUseProgram(textureShaderProgramId) }
		checkForError { -> glBindVertexArray(texture.vertexArrayId) }
		checkForError { -> glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0) }
	}

	@Override
	MappedTexture mapTexture(Texture texture, Rectanglef surface, float repeatX = 1, float repeatY = 1, boolean flipVertical = true) {

		def vertexArrayId = checkForError { -> glGenVertexArrays() }
		checkForError { -> glBindVertexArray(vertexArrayId) }

		def floatsPerVertex = Vector2f.FLOATS + Vector2f.FLOATS
		def bytesPerVertex = floatsPerVertex * Float.BYTES

		def verticesBuffer = FloatBuffer
			.allocateDirectNative(floatsPerVertex * 4)
			.put([
				0, flipVertical ? repeatY : 0,       surface.minX, surface.minY,
				0, flipVertical ? 0 : repeatY,       surface.minX, surface.maxY,
				repeatX, flipVertical ? 0 : repeatY, surface.maxX, surface.maxY,
				repeatX, flipVertical ? repeatY : 0, surface.maxX, surface.minY
			] as float[])
			.flip()

		def bufferId = glGenBuffers()
		checkForError { -> glBindBuffer(GL_ARRAY_BUFFER, bufferId) }
		checkForError { -> glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW) }

		// The above is unique vertices for a rectangle, but to draw a rectangle we
		// need to draw 2 triangles that share 2 vertices, so generate an element
		// buffer mapping triangle points to the above.
		def elementBufferId = glGenBuffers()
		checkForError { -> glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, elementBufferId) }
		checkForError { -> glBufferData(GL_ELEMENT_ARRAY_BUFFER, [0, 1, 3, 1, 2, 3] as int[], GL_STATIC_DRAW) }

		def texCoordAttrib = checkForError { -> glGetAttribLocation(textureShaderProgramId, 'texCoord') }
		checkForError { -> glEnableVertexAttribArray(texCoordAttrib) }
		checkForError { -> glVertexAttribPointer(texCoordAttrib, 2, GL_FLOAT, false, bytesPerVertex, 0) }
		def positionAttribute = checkForError { -> glGetAttribLocation(textureShaderProgramId, 'position') }
		checkForError { -> glEnableVertexAttribArray(positionAttribute) }
		checkForError { -> glVertexAttribPointer(positionAttribute, 2, GL_FLOAT, false, bytesPerVertex, Vector2f.BYTES) }

		// TODO: Move this to be handled by the Texture class?
		def mappedTexture = new MappedTexture(
			parentTexture: texture,
			vertexArrayId: vertexArrayId,
			bufferId: bufferId,
			elementBufferId: elementBufferId
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
	}
}
