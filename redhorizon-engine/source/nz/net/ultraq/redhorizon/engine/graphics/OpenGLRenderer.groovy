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

import nz.net.ultraq.redhorizon.events.EventTarget
import nz.net.ultraq.redhorizon.filetypes.Palette
import static nz.net.ultraq.redhorizon.filetypes.ColourFormat.*

import org.joml.Matrix4f
import org.joml.Rectanglef
import org.joml.Vector2f
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GLCapabilities
import org.lwjgl.opengl.GLDebugMessageCallback
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.opengl.GL41C.*
import static org.lwjgl.opengl.KHRDebug.*
import static org.lwjgl.system.MemoryStack.stackPush

import groovy.transform.Memoized
import java.nio.ByteBuffer

/**
 * A graphics renderer utilizing the modern OpenGL API, version 4.1.
 * 
 * @author Emanuel Rabina
 */
class OpenGLRenderer implements GraphicsRenderer, AutoCloseable, EventTarget {

	private static final Logger logger = LoggerFactory.getLogger(OpenGLRenderer)

	protected static final RendererEvent drawEvent = new DrawEvent()
	protected static final RendererEvent meshCreatedEvent = new MeshCreatedEvent()
	protected static final RendererEvent textureCreatedEvent = new TextureCreatedEvent()

	protected final GraphicsConfiguration config
	protected final GLCapabilities capabilities

	protected final List<Shader> shaders = []
	protected final Shader primitiveShader
	protected final Shader textureShader
	protected final Shader paletteShader

	private OpenGLBatchRenderer batchRenderer

	/**
	 * Constructor, create a modern OpenGL renderer with a set of defaults for Red
	 * Horizon's 2D game engine.
	 * 
	 * @param context
	 * @param config
	 */
	OpenGLRenderer(OpenGLContext context, GraphicsConfiguration config) {

		this.config = config
		capabilities = GL.createCapabilities()

		if (config.debug && capabilities.GL_KHR_debug) {
			glEnable(GL_DEBUG_OUTPUT)
			glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS)
			glDebugMessageCallback(new GLDebugMessageCallback() {
				@Override
				void invoke(int source, int type, int id, int severity, int length, long message, long userParam) {
					if (severity != GL_DEBUG_SEVERITY_NOTIFICATION) {
						throw new Exception("OpenGL error: ${getMessage(length, message)}")
					}
				}
			}, 0)
		}

		def clearColour = config.clearColour
		glClearColor(clearColour.r, clearColour.g, clearColour.b, clearColour.a)

		// Depth testing
		glEnable(GL_DEPTH_TEST)
		glDepthFunc(GL_LEQUAL)

		// Blending and blending function
		glEnable(GL_BLEND)
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

		// Set up the viewport
		def viewportSize = context.framebufferSize
		logger.debug('Establishing a viewport of size {}', viewportSize)
		glViewport(0, 0, viewportSize.width, viewportSize.height)
//		context.on(FramebufferSizeEvent) { event ->
//			logger.debug('Updating viewport to size {}x{}', event.width, event.height)
//			glViewport(0, 0, event.width, event.height)
//		}

		// Create the shader programs used by this renderer
		primitiveShader = createShader('Primitive')
		textureShader = createShader('Texture')
		paletteShader = createShader('TexturePalette')
	}

	/**
	 * Check for any OpenGL errors created by the OpenGL call in the given
	 * closure, throwing them if they occur.
	 * 
	 * @param closure
	 * @return
	 */
	protected static <T> T checkForError(Closure<T> closure) {

		def error

		// Clear any existing errors out
		do {
			error = glGetError()
		}
		while (error != GL_NO_ERROR)

		def result = closure()
		error = glGetError()
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

		checkForError { -> glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT) }
	}

	@Override
	void close() {

		shaders.each { shader ->
			glDeleteProgram(shader.programId)
		}
	}

	@Override
	void createCamera(Matrix4f projection, Matrix4f view) {

		stackPush().withCloseable { stack ->
			// TODO: Use a uniform buffer object to share these values across shaders
			shaders.each { shader ->
				def projectionBuffer = projection.get(stack.mallocFloat(Matrix4f.FLOATS))
				def projectionLocation = getUniformLocation(shader, 'projection')
				glProgramUniformMatrix4fv(shader.programId, projectionLocation, false, projectionBuffer)
				def viewBuffer = view.get(stack.mallocFloat(Matrix4f.FLOATS))
				def viewLocation = getUniformLocation(shader, 'view')
				glProgramUniformMatrix4fv(shader.programId, viewLocation, false, viewBuffer)
			}
		}
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
	Material createMaterial(Mesh mesh, Texture texture = null, ShaderType shaderType = null) {

		return new Material(
			mesh: mesh,
			texture: texture,
			shader:
				shaderType == ShaderType.TEXTURE ? textureShader :
				shaderType == ShaderType.TEXTURE_PALETTE ? paletteShader :
				primitiveShader
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

		return stackPush().withCloseable { stack ->
			return checkForError { ->
				def vertexArrayId = glGenVertexArrays()
				glBindVertexArray(vertexArrayId)

				def vertexBufferId = glGenBuffers()
				glBindBuffer(GL_ARRAY_BUFFER, vertexBufferId)

				def vertexBufferLayout = setVertexBufferLayout(primitiveShader,
					VertexBufferLayoutParts.COLOUR,
					VertexBufferLayoutParts.POSITION
				)

				def vertexBuffer = stack.mallocFloat(vertexBufferLayout.size() * vertices.length)
				vertices.each { vertex ->
					vertexBuffer
						.put(colour.r, colour.g, colour.b, colour.a)
						.put(vertex.x, vertex.y)
				}
				vertexBuffer.flip()
				glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW)

				trigger(meshCreatedEvent)

				return new Mesh(
					vertexArrayId: vertexArrayId,
					vertexBufferId: vertexBufferId,
					vertexType: vertexType,
					vertexCount: vertices.length,
					vertexBufferLayout: vertexBufferLayout
				)
			}
		}
	}

	/**
	 * Create a new shader program for the shader sources of the given name.
	 * 
	 * @param name
	 * @return
	 */
	private Shader createShader(String name) {

		/* 
		 * Create a shader of the specified name and type, running a compilation
		 * check to make sure it all went OK.
		 */
		def createShader = { int type ->
			def shaderPath = "nz/net/ultraq/redhorizon/engine/graphics/${name}.${type == GL_VERTEX_SHADER ? 'vert' : 'frag'}.glsl"
			def shaderSource = getResourceAsStream(shaderPath).withBufferedStream { stream ->
				return stream.text
			}
			def shaderId = glCreateShader(type)
			glShaderSource(shaderId, shaderSource)
			glCompileShader(shaderId)

			def status = glGetShaderi(shaderId, GL_COMPILE_STATUS)
			if (status != GL_TRUE) {
				throw new Exception(glGetShaderInfoLog(shaderId))
			}

			return shaderId
		}

		/* 
		 * Link multiple shader parts together into a shader program.
		 */
		def createProgram = { int vertexShaderId, int fragmentShaderId ->
			def programId = glCreateProgram()
			glAttachShader(programId, vertexShaderId)
			glAttachShader(programId, fragmentShaderId)
			glLinkProgram(programId)
			glValidateProgram(programId)

			def status = glGetProgrami(programId, GL_LINK_STATUS)
			if (status != GL_TRUE) {
				throw new Exception(glGetProgramInfoLog(programId))
			}

			return programId
		}

		def vertexShaderId = createShader(GL_VERTEX_SHADER)
		def fragmentShaderId = createShader(GL_FRAGMENT_SHADER)
		def programId = createProgram(vertexShaderId, fragmentShaderId)
		glDeleteShader(vertexShaderId)
		glDeleteShader(fragmentShaderId)

		def shader = new Shader(
			name: name,
			programId: programId
		)
		shaders << shader
		return shader
	}

	@Override
	Mesh createSpriteMesh(Rectanglef surface, float repeatX = 1, float repeatY = 1) {

		return stackPush().withCloseable { stack ->
			return checkForError { ->
				def vertexArrayId = glGenVertexArrays()
				glBindVertexArray(vertexArrayId)

				def vertexBufferId = glGenBuffers()
				glBindBuffer(GL_ARRAY_BUFFER, vertexBufferId)
				// NOTE: This is using the texture shader, but it could just as easily
				//       be the palette one too.  Currently they have the same layout so
				//       it works 😅  See my note in the method being called about this.
				def vertexBufferLayout = setVertexBufferLayout(textureShader,
					VertexBufferLayoutParts.COLOUR,
					VertexBufferLayoutParts.POSITION,
					VertexBufferLayoutParts.TEXCOORD
				)
				def vertexBuffer = stack.mallocFloat(vertexBufferLayout.size() * 4)
					.put(
						// Colour   // Position                 // Texture
						1, 1, 1, 1, surface.minX, surface.minY, 0,       0,
						1, 1, 1, 1, surface.minX, surface.maxY, 0,       repeatY,
						1, 1, 1, 1, surface.maxX, surface.maxY, repeatX, repeatY,
						1, 1, 1, 1, surface.maxX, surface.minY, repeatX, 0
					)
					.flip()
				glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW)

				// The above is unique vertices for a rectangle, but to draw a rectangle we
				// need to draw 2 triangles that share 2 vertices, so generate an element
				// buffer mapping triangle points to the above.
				def elementBufferId = glGenBuffers()
				glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, elementBufferId)
				def indexBuffer = stack.mallocInt(6)
					.put(0, 1, 3, 1, 2, 3)
					.flip()
				glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW)

				trigger(meshCreatedEvent)

				return new Mesh(
					vertexArrayId: vertexArrayId,
					vertexBufferId: vertexBufferId,
					vertexBufferLayout: vertexBufferLayout,
					elementBufferId: elementBufferId,
					elementType: GL_TRIANGLES,
					elementCount: 6
				)
			}
		}
	}

	@Override
	Texture createTexture(ByteBuffer data, int format, int width, int height, boolean filter = config.filter) {

		return stackPush().withCloseable { stack ->
			return checkForError { ->
				int textureId = glGenTextures()
				glBindTexture(GL_TEXTURE_2D, textureId)
				glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filter ? GL_LINEAR : GL_NEAREST)
				glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filter ? GL_LINEAR : GL_NEAREST)

				def colourFormat =
					format == 1 ? GL_RED :
					format == 3 ? GL_RGB :
					format == 4 ? GL_RGBA :
					0
				def textureBuffer = stack.malloc(data.remaining())
					.put(data.array(), data.position(), data.remaining())
					.flip()
			def matchesAlignment = (width * format) % 4 == 0
			if (!matchesAlignment) {
				checkForError { -> glPixelStorei(GL_UNPACK_ALIGNMENT, 1) }
			}
				glTexImage2D(GL_TEXTURE_2D, 0, colourFormat, width, height, 0, colourFormat, GL_UNSIGNED_BYTE, textureBuffer)
			if (!matchesAlignment) {
				checkForError { -> glPixelStorei(GL_UNPACK_ALIGNMENT, 4) }
			}

				trigger(textureCreatedEvent)

				return new Texture(
					textureId: textureId
				)
			}
		}
	}

	@Override
	Texture createTexturePalette(Palette palette) {

		return stackPush().withCloseable { stack ->
			return checkForError { ->
				int textureId = glGenTextures()
				glBindTexture(GL_TEXTURE_1D, textureId)
				glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
				glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)

				def colourFormat =
					palette.format == FORMAT_RGB ? GL_RGB :
					palette.format == FORMAT_RGBA ? GL_RGBA :
					0
				def paletteBuffer = stack.malloc(palette.size * palette.format.value)
				palette.size.times {i ->
					paletteBuffer.put(palette[i])
				}
				paletteBuffer.flip()
				glTexImage1D(GL_TEXTURE_1D, 0, colourFormat, palette.size, 0, colourFormat, GL_UNSIGNED_BYTE, paletteBuffer)

				trigger(textureCreatedEvent)

				return new Texture(
					textureId: textureId
				)
			}
		}
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
			glDeleteBuffers(mesh.elementBufferId)
		}
		glDeleteBuffers(mesh.vertexBufferId)
		glDeleteVertexArrays(mesh.vertexArrayId)
	}

	@Override
	void deleteTexture(Texture texture) {

		glDeleteTextures(texture.textureId)
	}

	@Override
	void drawMaterial(Material material) {

		averageNanos('drawMaterial', 1f, logger) { ->
			stackPush().withCloseable { stack ->
				checkForError { ->
					def mesh = material.mesh
					def texture = material.texture
					def shader = material.shader
					def model = material.model

					glUseProgram(shader.programId)
					if (texture) {
						checkForError { ->
							def texturesLocation = getUniformLocation(shader, 'u_textures')
							glUniform1iv(texturesLocation, 0)
							glActiveTexture(GL_TEXTURE0)
							glBindTexture(GL_TEXTURE_2D, texture.textureId)
						}
					}

					def modelBuffer = model.get(stack.mallocFloat(Matrix4f.FLOATS))
					def modelLocation = checkForError { -> getUniformLocation(shader, 'model') }
					checkForError { -> glUniformMatrix4fv(modelLocation, false, modelBuffer) }

					checkForError { -> glBindVertexArray(mesh.vertexArrayId) }
					if (mesh.vertexType) {
						checkForError { -> glDrawArrays(mesh.vertexType, 0, mesh.vertexCount) }
					}
					else if (mesh.elementType) {
						checkForError { -> glDrawElements(mesh.elementType, mesh.elementCount, GL_UNSIGNED_INT, 0) }
					}
					trigger(drawEvent)

//					if (texture) {
//						glActiveTexture(GL_TEXTURE1)
//						glBindTexture(GL_TEXTURE_2D, 0)
//					}
//					glUseProgram(0)
				}
			}
		}
	}

	/**
	 * Cached function for looking up an attribute location in a shader program.
	 * 
	 * @param shader
	 * @param name
	 * @return
	 */
	@Memoized
	protected static int getAttribLocation(Shader shader, String name) {

		return glGetAttribLocation(shader.programId, name)
	}

	/**
	 * Cached function for looking up a uniform location in a shader program.
	 * 
	 * @param shader
	 * @param name
	 * @return
	 */
	@Memoized
	protected static int getUniformLocation(Shader shader, String name) {

		return glGetUniformLocation(shader.programId, name)
	}

	@Override
	void setPalette(Texture palette) {

		def paletteLocation = getUniformLocation(paletteShader, 'u_palette')
		glProgramUniform1i(paletteShader.programId, paletteLocation, 15)
		glActiveTexture(GL_TEXTURE15)
		glBindTexture(GL_TEXTURE_1D, palette.textureId)
//		glActiveTexture(GL_TEXTURE0)
//		glBindTexture(GL_TEXTURE_1D, 0)
	}

	/**
	 * Creates an object describing the layout of the currently-bound vertex
	 * buffer for use with the given shader.
	 * 
	 * @param shader
	 * @param parts
	 * @return
	 */
	protected static VertexBufferLayout setVertexBufferLayout(Shader shader, VertexBufferLayoutParts... parts) {

		// TODO: Right now, I think this only works is because the inputs are all in
		//       the same order!  In which case, maybe I can reduce the number of
		//       shader programs again or create a map between layout positions and
		//       input names.
		def layout = new VertexBufferLayout(parts)
		def stride = layout.sizeInBytes()
		parts.each { part ->
			def location = getAttribLocation(shader, part.name)
			if (location == -1) {
				throw new Exception("Component ${part.name} isn't a part of the ${shader}")
			}
			glEnableVertexAttribArray(location)
			glVertexAttribPointer(location, part.size, GL_FLOAT, false, stride, layout.offsetOfInBytes(part))
		}
		return layout
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
	void updateCamera(Matrix4f view) {

		stackPush().withCloseable { stack ->
			// TODO: Use a uniform buffer object to share these values across shaders
			shaders.each { shader ->
				def viewBuffer = view.get(stack.mallocFloat(Matrix4f.FLOATS))
				def viewLocation = getUniformLocation(shader, 'view')
				glProgramUniformMatrix4fv(shader.programId, viewLocation, false, viewBuffer)
			}
		}
	}

	@Override
	void withBatchRenderer(ShaderType shaderType, Matrix4f modelMatrix, Closure closure) {

		if (!batchRenderer) {
			batchRenderer = new OpenGLBatchRenderer(this)
			batchRenderer.on(RendererEvent) { event ->
				trigger(event)
			}
		}

		// TODO: Remove these restrictions on the batch
		batchRenderer.shader =
			shaderType == ShaderType.TEXTURE ? textureShader :
			shaderType == ShaderType.TEXTURE_PALETTE ? paletteShader :
			primitiveShader
		batchRenderer.modelMatrix = modelMatrix

		closure(batchRenderer)
	}
}
