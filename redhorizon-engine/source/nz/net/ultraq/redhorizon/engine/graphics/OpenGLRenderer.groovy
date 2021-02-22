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

import static nz.net.ultraq.redhorizon.filetypes.ColourFormat.FORMAT_RGBA

import org.joml.Matrix4f
import org.joml.Rectanglef
import org.joml.Vector2f
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GLCapabilities
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.opengl.GL33C.*

import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer

/**
 * A graphics renderer utilizing the modern OpenGL API, so OpenGL 3.3+ and
 * shaders only.
 * 
 * @author Emanuel Rabina
 */
class OpenGLRenderer implements GraphicsRenderer, AutoCloseable {

	private static final Logger logger = LoggerFactory.getLogger(OpenGLRenderer)

	protected final GLCapabilities capabilities
	protected final Colour clearColour
	protected final boolean filter

	private Texture mockTexture
	private Shader standardShader

	/**
	 * Constructor, create a modern OpenGL renderer with a set of defaults for Red
	 * Horizon's 2D game engine.
	 * 
	 * @param context
	 * @param config
	 */
	OpenGLRenderer(OpenGLContext context, GraphicsConfiguration config) {

		capabilities = GL.createCapabilities()

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
		filter = config.filter

		glClearColor(clearColour.r, clearColour.g, clearColour.b, 1)

		// Edge smoothing
		glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)
		glHint(GL_POLYGON_SMOOTH_HINT, GL_NICEST)

		// Depth testing
		checkForError { -> glEnable(GL_DEPTH_TEST) }
		checkForError { -> glDepthFunc(GL_LEQUAL) }

		// Blending and blending function
		checkForError { -> glEnable(GL_BLEND) }
		checkForError { -> glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA) }

		// Set up the viewport
		def viewportSize = context.framebufferSize
		logger.debug('Establishing a viewport of size {}', viewportSize)
		checkForError { -> glViewport(0, 0, viewportSize.width, viewportSize.height) }
//		context.on(FramebufferSizeEvent) { event ->
//			logger.debug('Updating viewport to size {}x{}', event.width, event.height)
//			glViewport(0, 0, event.width, event.height)
//		}

		// Generate the 1x1 white 'mock texture' used by the shader for rendering primitives
		mockTexture = createTexture(ByteBuffer.wrapNative(Colour.WHITE as byte[]), FORMAT_RGBA.value, 1, 1)

		// Create the shader programs used by this renderer
		standardShader = createShader('Standard')
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
				error == GL_INVALID_FRAMEBUFFER_OPERATION ? 'GL_INVALID_FRAMEBUFFER_OPERATION' :
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
	void close() {

		glDeleteProgram(standardShader.programId)
	}

	@Override
	void createCamera(Matrix4f projection) {

		checkForError { -> glUseProgram(standardShader.programId) }
		def projectionUniform = checkForError { ->
			return glGetUniformLocation(standardShader.programId, 'projection')
		}
		checkForError { ->
			glUniformMatrix4fv(projectionUniform, false, projection as float[])
		}
		checkForError { -> glUseProgram(0) }
	}

	@Override
	Mesh createLineLoopMesh(Colour colour, Vector2f... vertices) {

		return createPrimitivesMesh(colour, GL_LINE_LOOP, vertices)
	}

	@Override
	Mesh createLinesMesh(Colour colour, Vector2f... vertices) {

		return createPrimitivesMesh(colour, GL_LINES, vertices)
	}

	@Override
	Material createMaterial(Mesh mesh, Texture texture) {

		return new Material(
			mesh: mesh,
			texture: texture
		)
	}

	/**
	 * Create a mesh used for drawing some kind of OpenGL primitive.
	 * 
	 * @param colour
	 * @param vertexType
	 * @param vertices
	 * @return
	 */
	private Mesh createPrimitivesMesh(Colour colour, int vertexType, Vector2f... vertices) {

		def vertexArrayId = checkForError { -> glGenVertexArrays() }
		checkForError { -> glBindVertexArray(vertexArrayId) }

		def floatsPerVertex = Colour.FLOATS + Vector2f.FLOATS + Vector2f.FLOATS
		def verticesBuffer = FloatBuffer.allocateDirectNative(floatsPerVertex * vertices.length)
		vertices.each { vertex ->
			verticesBuffer
				.put(colour as float[])
				.put(vertex as float[])
				.put([0, 0] as float[])
		}
		verticesBuffer.flip()

		def vertexBufferId = glGenBuffers()
		checkForError { -> glBindBuffer(GL_ARRAY_BUFFER, vertexBufferId) }
		checkForError { -> glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW) }

		setVertexBufferLayout()

		return new Mesh(
			vertexArrayId: vertexArrayId,
			vertexBufferId: vertexBufferId,
			vertexType: vertexType,
			vertexCount: vertices.length
		)
	}

	/**
	 * Create a new shader program for the shader sources of the given name.
	 * 
	 * @param vertexShaderSource
	 * @param fragmentShaderSource
	 * @return
	 */
	private Shader createShader(String shaderType) {

		/**
		 * Create a shader of the specified type, running a compilation check to
		 * make sure it all went OK.
		 */
		def createShader = { int type, String source ->
			def shaderId = glCreateShader(type)
			checkForError { -> glShaderSource(shaderId, source) }
			checkForError { -> glCompileShader(shaderId) }

			def status = glGetShaderi(shaderId, GL_COMPILE_STATUS)
			if (status != GL_TRUE) {
				throw new RuntimeException(glGetShaderInfoLog(shaderId))
			}

			return shaderId
		}

		/**
		 * Link multiple shader parts together into a shader program.
		 */
		def createProgram = { int vertexShaderId, int fragmentShaderId ->
			def programId = checkForError { -> glCreateProgram() }
			checkForError { -> glAttachShader(programId, vertexShaderId) }
			checkForError { -> glAttachShader(programId, fragmentShaderId) }
			checkForError { -> glLinkProgram(programId) }
			checkForError { -> glValidateProgram(programId) }

			def status = glGetProgrami(programId, GL_LINK_STATUS)
			if (status != GL_TRUE) {
				throw new RuntimeException(glGetProgramInfoLog(programId))
			}

			return programId
		}

		def vertexShaderId = getResourceAsStream("nz/net/ultraq/redhorizon/engine/graphics/${shaderType}.vert").withBufferedStream { stream ->
			return createShader(GL_VERTEX_SHADER, stream.text)
		}
		def fragmentShaderId = getResourceAsStream("nz/net/ultraq/redhorizon/engine/graphics/${shaderType}.frag").withBufferedStream { stream ->
			return createShader(GL_FRAGMENT_SHADER, stream.text)
		}
		def programId = createProgram(vertexShaderId, fragmentShaderId)
		checkForError { -> glDeleteShader(vertexShaderId) }
		checkForError { -> glDeleteShader(fragmentShaderId) }

		return new Shader(
			programId: programId
		)
	}

	@Override
	Mesh createSpriteMesh(Rectanglef surface, float repeatX = 1, float repeatY = 1) {

		def vertexArrayId = checkForError { -> glGenVertexArrays() }
		checkForError { -> glBindVertexArray(vertexArrayId) }

		def floatsPerVertex = Colour.FLOATS + Vector2f.FLOATS + Vector2f.FLOATS
		def verticesBuffer = FloatBuffer
			.allocateDirectNative(floatsPerVertex * 4)
			.put([
				// Colour   // Position                 // Texture
				1, 1, 1, 1, surface.minX, surface.minY, 0,       0,
				1, 1, 1, 1, surface.minX, surface.maxY, 0,       repeatY,
				1, 1, 1, 1, surface.maxX, surface.maxY, repeatX, repeatY,
				1, 1, 1, 1, surface.maxX, surface.minY, repeatX, 0,
			] as float[])
			.flip()

		def vertexBufferId = glGenBuffers()
		checkForError { -> glBindBuffer(GL_ARRAY_BUFFER, vertexBufferId) }
		checkForError { -> glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW) }

		// The above is unique vertices for a rectangle, but to draw a rectangle we
		// need to draw 2 triangles that share 2 vertices, so generate an element
		// buffer mapping triangle points to the above.
		def indexBuffer = IntBuffer
			.allocateDirectNative(6)
			.put([0, 1, 3, 1, 2, 3] as int[])
			.flip()

		def elementBufferId = glGenBuffers()
		checkForError { -> glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, elementBufferId) }
		checkForError { -> glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW) }

		setVertexBufferLayout()

		return new Mesh(
			vertexArrayId: vertexArrayId,
			vertexBufferId: vertexBufferId,
			elementBufferId: elementBufferId,
			elementType: GL_TRIANGLES,
			elementCount: indexBuffer.capacity()
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
	void deleteMaterial(Material material) {

		deleteMesh(material.mesh)
		if (material.texture) {
			deleteTexture(material.texture)
		}
	}

	@Override
	void deleteMesh(Mesh mesh) {

		if (mesh.elementBufferId) {
			checkForError { -> glDeleteBuffers(mesh.elementBufferId) }
		}
		checkForError { -> glDeleteBuffers(mesh.vertexBufferId) }
		checkForError { -> glDeleteVertexArrays(mesh.vertexArrayId) }
	}

	@Override
	void deleteTexture(Texture texture) {

		checkForError { ->
			glDeleteTextures(texture.textureId)
		}
	}

	@Override
	void drawMaterial(Material material) {

		averageNanos('drawMaterial', 2f) { ->
			def mesh = material.mesh
			def texture = material.texture
			def model = material.model

			checkForError { -> glBindTexture(GL_TEXTURE_2D, (texture ?: mockTexture).textureId) }
			checkForError { -> glUseProgram(standardShader.programId) }

			def modelLocation = checkForError { -> glGetUniformLocation(standardShader.programId, 'model') }
			checkForError { -> glUniformMatrix4fv(modelLocation, false, model as float[]) }
			checkForError { -> glBindVertexArray(mesh.vertexArrayId) }
			if (mesh.vertexType) {
				checkForError { -> glDrawArrays(mesh.vertexType, 0, mesh.vertexCount) }
			}
			else if (mesh.elementType) {
				checkForError { -> glDrawElements(mesh.elementType, mesh.elementCount, GL_UNSIGNED_INT, 0) }
			}

			checkForError { -> glUseProgram(0) }
			checkForError { -> glBindTexture(GL_TEXTURE_2D, 0) }
		}
	}

	/**
	 * Sets the layout of the currently-bound vertex buffer data for use with the
	 * standard shader.
	 */
	private void setVertexBufferLayout() {

		def floatsPerVertex = Colour.FLOATS + Vector2f.FLOATS + Vector2f.FLOATS
		def bytesPerVertex = floatsPerVertex * Float.BYTES

		def colourAttrib = checkForError { -> glGetAttribLocation(standardShader.programId, 'colour') }
		checkForError { -> glEnableVertexAttribArray(colourAttrib) }
		checkForError { -> glVertexAttribPointer(colourAttrib, 4, GL_FLOAT, false, bytesPerVertex, 0) }

		def positionAttribute = checkForError { -> glGetAttribLocation(standardShader.programId, 'position') }
		checkForError { -> glEnableVertexAttribArray(positionAttribute) }
		checkForError { -> glVertexAttribPointer(positionAttribute, 2, GL_FLOAT, false, bytesPerVertex, Colour.BYTES) }

		def texCoordAttrib = checkForError { -> glGetAttribLocation(standardShader.programId, 'texCoord') }
		checkForError { -> glEnableVertexAttribArray(texCoordAttrib) }
		checkForError { -> glVertexAttribPointer(texCoordAttrib, 2, GL_FLOAT, false, bytesPerVertex, Colour.BYTES + Vector2f.BYTES) }
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
	void updateCamera(Matrix4f projection) {

		checkForError { -> glUseProgram(standardShader.programId) }
		def viewLocation = checkForError { -> glGetUniformLocation(standardShader.programId, 'view') }
		checkForError { -> glUniformMatrix4fv(viewLocation, false, projection as float[]) }
	}
}
