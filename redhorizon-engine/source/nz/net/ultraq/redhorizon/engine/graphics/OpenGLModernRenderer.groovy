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
import org.lwjgl.opengl.GL
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
class OpenGLModernRenderer implements GraphicsRenderer {

	private static final Logger logger = LoggerFactory.getLogger(OpenGLModernRenderer)

	// Configuration values
	private final Colour clearColour
	private final boolean filter

	/**
	 * Constructor, create a modern OpenGL renderer with a set of defaults for Red
	 * Horizon's 2D game engine.
	 * 
	 * @param context
	 * @param config
	 */
	OpenGLModernRenderer(OpenGLContext context, GraphicsConfiguration config) {

		GL.createCapabilities()

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

		clearColour = config.clearColour
		glClearColor(clearColour.r, clearColour.g, clearColour.b, 1)

		// Edge smoothing
		filter = config.filter
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
//		logger.debug('Establishing a viewport of size {}', viewportSize)
//		checkForError { -> glViewport(0, 0, viewportSize.width, viewportSize.height) }
//		context.on(FramebufferSizeEvent) { event ->
//			logger.debug('Updating viewport to size {}x{}', event.width, event.height)
//			glViewport(0, 0, event.width, event.height)
//		}
	}

	void createLinesBuffer(Colour colour, Vector2f... vertices) {

//		def layoutSize = Colour.BYTES + Vector2f.BYTES
		def layoutSize = Vector2f.BYTES

		def verticesBuffer = FloatBuffer.allocateDirectNative(layoutSize * vertices.length)
		vertices.each { vertex ->
//			verticesBuffer.put(colour as float[])
			verticesBuffer.put(vertex as float[])
		}
		verticesBuffer.flip()

		def bufferId = glGenBuffers()
		glBindBuffer(GL_ARRAY_BUFFER, bufferId)
		glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW)

//		glEnableVertexAttribArray(0)
//		glVertexAttribPointer(0, 4, GL_FLOAT, false, layoutSize, 0)
		glEnableVertexAttribArray(0)
		glVertexAttribPointer(0, 2, GL_FLOAT, false, layoutSize, 0)

//		return bufferId
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

	int createFragmentShader(String shaderSource) {

		return createShader(GL_FRAGMENT_SHADER, shaderSource)
	}

	int createProgram(int... shaderIds) {

		return checkProgramLink { ->
			def shaderProgram = glCreateProgram()
			shaderIds.each { shaderId ->
				glAttachShader(shaderProgram, shaderId)
			}
//			glBindFragDataLocation(shaderProgram, 0, 'fragColor') // TODO: 🤔
			glLinkProgram(shaderProgram)
//			glValidateProgram(shaderProgram)
			glUseProgram(shaderProgram)
			programId = shaderProgram
			return shaderProgram
		}
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
			glShaderSource(shaderId, shaderSource)
			glCompileShader(shaderId)
			return shaderId
		}
	}

	int createVertexShader(String shaderSource) {

		return createShader(GL_VERTEX_SHADER, shaderSource)
	}

	void deleteArrays(int... arrayIds) {

		glDeleteVertexArrays(arrayIds)
	}

	void deleteBuffers(int... bufferIds) {

		glDeleteBuffers(bufferIds)
	}

	void deleteShaders(int... shaderIds) {

		shaderIds.each { shaderId ->
			glDeleteShader(shaderId)
		}
	}

	void deletePrograms(int... programIds) {

		programIds.each { programId ->
			glDeleteProgram(programId)
		}
	}

	void runProgram(int programId, int bufferId) {

//		glBindBuffer(GL_ARRAY_BUFFER, bufferId)
//		glUseProgram(programId)
		glDrawArrays(GL_TRIANGLES, 0, 3)
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
				error == GL_INVALID_FRAMEBUFFER_OPERATION ? 'GL_INVALID_FRAMEBUFFER_OPERATION' :
				error == GL_OUT_OF_MEMORY ? 'GL_OUT_OF_MEMORY' :
				error == GL_STACK_UNDERFLOW ? 'GL_STACK_UNDERFLOW' :
				error == GL_STACK_OVERFLOW ? 'GL_STACK_OVERFLOW' :
				error
			logger.error("OpenGL error: ${errorCode}")
			throw new Exception("OpenGL error: ${errorCode}")
		}
		return result
	}

	@Override
	void clear() {

		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)
//		checkForError { -> glClear(GL_STENCIL_BUFFER_BIT) }
	}

	@Override
	void createCamera(Rectanglef projection) {
	}

	@Override
	int createTexture(ByteBuffer data, int format, int width, int height) {

		return createTexture(data, format, width, height, filter)
	}

	@Override
	int createTexture(ByteBuffer data, int format, int width, int height, boolean filter) {

		int textureId = glGenTextures()
		glBindTexture(GL_TEXTURE_2D, textureId)
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filter ? GL_LINEAR : GL_NEAREST)
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filter ? GL_LINEAR : GL_NEAREST)
		def colourFormat =
			format == 3 ? GL_RGB :
			format == 4 ? GL_RGBA :
			0
		glTexImage2D(GL_TEXTURE_2D, 0, colourFormat, width, height, 0, colourFormat, GL_UNSIGNED_BYTE, data)
		return textureId
	}

	@Override
	void deleteTextures(int... textureIds) {

		glDeleteTextures(textureIds)
	}

	@Override
	void drawLineLoop(Colour colour, Vector2f... vertices) {

		drawPrimitive(GL_LINE_LOOP, colour, vertices)
	}

	@Override
	void drawLines(Colour colour, Vector2f... vertices) {

		drawPrimitive(GL_LINES, colour, vertices)
	}

	/**
	 * Draw any kind of coloured primitive.
	 *
	 * @param primitiveType
	 * @param colour
	 * @param vertices
	 */
	private static void drawPrimitive(int primitiveType, Colour colour, Vector2f... vertices) {

//		withTextureEnvironmentMode(GL_COMBINE) { ->
//			checkForError { -> glColor4f(colour.r, colour.g, colour.b, colour.a) }
//			glBegin(primitiveType)
//			vertices.each { vertex ->
//				glVertex2f(vertex.x, vertex.y)
//			}
//			checkForError { -> glEnd() }
//		}
	}

	@Override
	void drawTexture(int textureId, Rectanglef rectangle, float repeatX = 1, float repeatY = 1, boolean flipVertical = true) {

		glBindTexture(GL_TEXTURE_2D, textureId)
		glColor3f(1, 1, 1)
		glBegin(GL_QUADS)
		glTexCoord2f(0, flipVertical ? repeatY : 0); glVertex2f(rectangle.minX, rectangle.minY)
		glTexCoord2f(0, flipVertical ? 0 : repeatY); glVertex2f(rectangle.minX, rectangle.maxY)
		glTexCoord2f(repeatX, flipVertical ? 0 : repeatY); glVertex2f(rectangle.maxX, rectangle.maxY)
		glTexCoord2f(repeatX, flipVertical ? repeatY : 0); glVertex2f(rectangle.maxX, rectangle.minY)
		glEnd()
	}

	/**
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
