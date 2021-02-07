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

import org.joml.Matrix4f
import org.joml.Rectanglef
import org.joml.Vector2f
import org.joml.Vector3f
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.opengl.GL33C.*

import java.nio.FloatBuffer
import java.nio.IntBuffer

/**
 * A graphics renderer using the OpenGL core profile, so OpenGL 3.3+ and shaders
 * only.
 * 
 * @author Emanuel Rabina
 */
class OpenGLModernRenderer extends OpenGLRenderer {

	private static final Logger logger = LoggerFactory.getLogger(OpenGLModernRenderer)

	private Shader linesShader
	private Shader textureShader

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

		// Create the shader programs used by this renderer
		linesShader = createShader('Default')
//		checkForError { -> glBindFragDataLocation(linesShader.shaderId, 0, 'vertexColour') }
		textureShader = createShader('Texture')
	}

	@Override
	void close() {

		glDeleteProgram(linesShader.programId)
		glDeleteProgram(textureShader.programId)
	}

	@Override
	void createCamera(Matrix4f projection) {

		checkForError { -> glUseProgram(linesShader.programId) }
		def linesProjectionUniform = checkForError { ->
			return glGetUniformLocation(linesShader.programId, 'projection')
		}
		checkForError { ->
			glUniformMatrix4fv(linesProjectionUniform, false, projection as float[])
		}

		checkForError { -> glUseProgram(textureShader.programId) }
		def textureProjectionUniform = checkForError { ->
			return glGetUniformLocation(textureShader.programId, 'projection')
		}
		checkForError { ->
			def projectionBuffer = FloatBuffer.allocateDirectNative(Matrix4f.FLOATS)
			projection.get(projectionBuffer)
			glUniformMatrix4fv(textureProjectionUniform, false, projectionBuffer)
		}
	}

	@Override
	Lines createLines(Colour colour, Vector2f... vertices) {

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

		def colourAttrib = checkForError { -> glGetAttribLocation(linesShader.programId, 'colour') }
		checkForError { -> glEnableVertexAttribArray(colourAttrib) }
		checkForError { -> glVertexAttribPointer(colourAttrib, 4, GL_FLOAT, false, bytesPerVertex, 0) }
		def positionAttrib = checkForError { -> glGetAttribLocation(linesShader.programId, 'position') }
		checkForError { -> glEnableVertexAttribArray(positionAttrib) }
		checkForError { -> glVertexAttribPointer(positionAttrib, 2, GL_FLOAT, false, bytesPerVertex, Colour.BYTES) }

		return new Lines(
			vertexArrayId: vertexArrayId,
			bufferId: bufferId,
			colour: colour,
			vertices: vertices
		)
	}

	@Override
	Material createMaterial(Mesh mesh, Texture texture) {

		return new Material(
			mesh: mesh,
			texture: texture,
			shader: textureShader
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

		def floatsPerVertex = Vector2f.FLOATS + Vector2f.FLOATS
		def bytesPerVertex = floatsPerVertex * Float.BYTES

		def verticesBuffer = FloatBuffer
			.allocateDirectNative(floatsPerVertex * 4)
			.put([
				0,       0,       surface.minX, surface.minY,
				0,       repeatY, surface.minX, surface.maxY,
				repeatX, repeatY, surface.maxX, surface.maxY,
				repeatX, 0,       surface.maxX, surface.minY
			] as float[])
			.flip()

		def bufferId = glGenBuffers()
		checkForError { -> glBindBuffer(GL_ARRAY_BUFFER, bufferId) }
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

		def texCoordAttrib = checkForError { -> glGetAttribLocation(textureShader.programId, 'texCoord') }
		checkForError { -> glEnableVertexAttribArray(texCoordAttrib) }
		checkForError { -> glVertexAttribPointer(texCoordAttrib, 2, GL_FLOAT, false, bytesPerVertex, 0) }
		def positionAttribute = checkForError { -> glGetAttribLocation(textureShader.programId, 'position') }
		checkForError { -> glEnableVertexAttribArray(positionAttribute) }
		checkForError { -> glVertexAttribPointer(positionAttribute, 2, GL_FLOAT, false, bytesPerVertex, Vector2f.BYTES) }

		return new Mesh(
			vertexArrayId: vertexArrayId,
			bufferId: bufferId,
			elementBufferId: elementBufferId,
			elementType: GL_TRIANGLES,
			elementCount: indexBuffer.capacity()
		)
	}

	@Override
	void deleteLines(Lines lines) {

		checkForError { -> glDeleteBuffers(lines.bufferId) }
		checkForError { -> glDeleteVertexArrays(lines.vertexArrayId) }
	}

	@Override
	void deleteMaterial(Material material) {

		deleteMesh(material.mesh)
		deleteTexture(material.texture)
	}

	@Override
	void deleteMesh(Mesh mesh) {

		checkForError { -> glDeleteBuffers(mesh.bufferId, mesh.elementBufferId) }
		checkForError { -> glDeleteVertexArrays(mesh.vertexArrayId) }
	}

	@Override
	void drawLineLoop(Colour colour, Vector2f... vertices) {
	}

	@Override
	void drawLines(Lines lines) {

		checkForError { -> glUseProgram(linesShader.programId) }
		checkForError { -> glBindVertexArray(lines.vertexArrayId) }
		checkForError { -> glDrawArrays(GL_LINES, 0, lines.vertices.length) }
	}

	@Override
	void drawMaterial(Material material) {

		checkForError { -> glUseProgram(material.shader.programId) }
		checkForError { -> glBindVertexArray(material.mesh.vertexArrayId) }
		checkForError { -> glDrawElements(material.mesh.elementType, material.mesh.elementCount, GL_UNSIGNED_INT, 0) }
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

		// TODO: Use a view matrix to make it look like we're moving the camera
	}
}
