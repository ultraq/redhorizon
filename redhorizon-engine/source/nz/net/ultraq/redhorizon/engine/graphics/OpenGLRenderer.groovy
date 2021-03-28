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
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import java.nio.ByteBuffer

/**
 * A graphics renderer utilizing the modern OpenGL API, version 4.1.
 * 
 * @author Emanuel Rabina
 */
class OpenGLRenderer implements GraphicsRenderer, AutoCloseable, EventTarget {

	private static final Logger logger = LoggerFactory.getLogger(OpenGLRenderer)

	protected static final VertexBufferLayout VERTEX_BUFFER_LAYOUT = new VertexBufferLayout(
		VertexBufferLayoutParts.COLOUR,
		VertexBufferLayoutParts.POSITION,
		VertexBufferLayoutParts.TEXCOORD,
		VertexBufferLayoutParts.TEXUNIT,
		VertexBufferLayoutParts.MODEL_INDEX
	)

	protected final GraphicsConfiguration config
	protected final GLCapabilities capabilities
	protected final int maxTextureUnits

	protected final List<Shader> shaders = []
	protected final Shader standardShader
	protected final Shader standardPaletteShader
	protected Texture whiteTexture

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

		// Set up hardware limits
		maxTextureUnits = glGetInteger(GL_MAX_TEXTURE_IMAGE_UNITS) - 1 // Last slot reserved for palette

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
		standardShader = createShader(ShaderType.STANDARD.name)
		standardPaletteShader = createShader(ShaderType.STANDARD_PALETTE.name)

		// The white texture used as a fallback when no texture is bound
		stackPush().withCloseable { stack ->
			def textureBytes = ByteBuffer.allocateNative(4)
				.putInt(0xffffffff as int)
				.flip()
			whiteTexture = createTexture(textureBytes, FORMAT_RGBA.value, 1, 1, false)
		}
	}

	@Override
	void asBatchRenderer(
		@ClosureParams(value = SimpleType, options = 'nz.net.ultraq.redhorizon.engine.graphics.BatchRenderer')
		Closure closure) {

		if (!batchRenderer) {
			batchRenderer = new OpenGLBatchRenderer(this)
			batchRenderer.on(RendererEvent) { event ->
				trigger(event)
			}
		}
		closure(batchRenderer)
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

		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)
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

		return createMeshData(createMesh(GL_LINE_LOOP, colour, vertices))
	}

	@Override
	Mesh createLinesMesh(Colour colour, Vector2f... vertices) {

		return createMeshData(createMesh(GL_LINES, colour, vertices))
	}

	@Override
	Material createMaterial(Mesh mesh, Texture texture = whiteTexture, ShaderType shaderType = ShaderType.STANDARD) {

		return new Material(
			mesh: mesh,
			texture: texture,
			shader: shaderType == ShaderType.STANDARD_PALETTE ? standardPaletteShader : standardShader
		)
	}

	/**
	 * Build a mesh object.
	 * 
	 * @param vertexType
	 * @param colour
	 *   The colour to use for all vertices.  Currently doesn't support different
	 *   colours for each vertex.
	 * @param vertices
	 * @param textureCoordinates
	 * @param indices
	 * @return
	 */
	protected Mesh createMesh(int vertexType, Colour colour, Vector2f[] vertices,
		Vector2f[] textureCoordinates = new Rectanglef() as Vector2f[], int[] indices = new int[0]) {

		def mesh = new Mesh(
			vertexType: vertexType,
			colour: colour,
			vertices: vertices,
			textureCoordinates: textureCoordinates,
			indices: indices
		)
		trigger(new MeshCreatedEvent(mesh))
		return mesh
	}

	/**
	 * Create buffers based on the given mesh data representing some kind of
	 * OpenGL primitive.
	 * 
	 * @param mesh
	 * @return
	 */
	private static Mesh createMeshData(Mesh mesh) {

		return stackPush().withCloseable { stack ->
			def vertexArrayId = glGenVertexArrays()
			glBindVertexArray(vertexArrayId)

			def vertexBufferId = glGenBuffers()
			glBindBuffer(GL_ARRAY_BUFFER, vertexBufferId)

			enableVertexBufferLayout(VERTEX_BUFFER_LAYOUT)

			// Buffer to hold all the vertex data
			def colour = mesh.colour
			def vertices = mesh.vertices
			def textureCoordinates = mesh.textureCoordinates
			def vertexBuffer = stack.mallocFloat(VERTEX_BUFFER_LAYOUT.size() * vertices.size())
			vertices.eachWithIndex { vertex, index ->
				vertexBuffer
					.put(colour.r, colour.g, colour.b, colour.a)
					.put(vertex.x, vertex.y)
				if (textureCoordinates) {
					def textureCoordinate = textureCoordinates[index]
					vertexBuffer.put(textureCoordinate.x, textureCoordinate.y)
				}
				vertexBuffer.put(0 as float, 0 as float)
			}
			vertexBuffer.flip()
			glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW)

			// Buffer for all the index data, if applicable
			int elementBufferId = 0
			if (mesh.indices) {
				elementBufferId = glGenBuffers()
				glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, elementBufferId)
				def indexBuffer = stack.mallocInt(mesh.indices.size())
					.put(mesh.indices)
					.flip()
				glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW)
			}

			mesh.vertexArrayId = vertexArrayId
			mesh.vertexBufferId = vertexBufferId
			mesh.vertexBufferLayout = vertexBufferLayout
			if (mesh.indices) {
				mesh.elementBufferId = elementBufferId
			}
			return mesh
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
		def createShader = { int type, Closure<String> mod = null ->
			def shaderPath = "nz/net/ultraq/redhorizon/engine/graphics/${name}.${type == GL_VERTEX_SHADER ? 'vert' : 'frag'}.glsl"
			def shaderSource = getResourceAsStream(shaderPath).withBufferedStream { stream ->
				return mod ? mod(stream.text) : stream.text
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

		def capTextureUnits = { source ->
			return source.replace('[maxTextureUnits]', "[${maxTextureUnits}]")
		}
		def vertexShaderId = createShader(GL_VERTEX_SHADER, capTextureUnits)
		def fragmentShaderId = createShader(GL_FRAGMENT_SHADER, capTextureUnits)
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

		return createMeshData(createMesh(
			GL_TRIANGLES,
			Colour.WHITE,
			surface as Vector2f[],
			new Rectanglef(0, 0, repeatX, repeatY) as Vector2f[],
			new int[]{ 0, 1, 3, 1, 2, 3 }
		))
	}

	@Override
	Texture createTexture(ByteBuffer data, int format, int width, int height, boolean filter = config.filter) {

		return stackPush().withCloseable { stack ->
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

			trigger(new TextureCreatedEvent())

			return new Texture(
				textureId: textureId
			)
		}
	}

	@Override
	Texture createTexturePalette(Palette palette) {

		return stackPush().withCloseable { stack ->
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

			def texture = new Texture(
				textureId: textureId
			)
			trigger(new TextureCreatedEvent(texture))
			return texture
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
		trigger(new MeshDeletedEvent(mesh))
	}

	@Override
	void deleteTexture(Texture texture) {

		glDeleteTextures(texture.textureId)
		trigger(new TextureDeletedEvent(texture))
	}

	@Override
	void drawMaterial(Material material, Matrix4f transform) {

		averageNanos('drawMaterial', 1f, logger) { ->
			stackPush().withCloseable { stack ->
				def mesh = material.mesh
				def texture = material.texture
				def shader = material.shader

				glUseProgram(shader.programId)
				if (texture) {
					def texturesLocation = getUniformLocation(shader, 'u_textures')
					glUniform1iv(texturesLocation, 0)
					glActiveTexture(GL_TEXTURE0)
					glBindTexture(GL_TEXTURE_2D, texture.textureId)
				}

				def modelsBuffer = transform.get(stack.mallocFloat(Matrix4f.FLOATS))
				def modelsLocation = getUniformLocation(shader, 'models')
				glUniformMatrix4fv(modelsLocation, false, modelsBuffer)

				glBindVertexArray(mesh.vertexArrayId)
				if (mesh.indices) {
					glDrawElements(mesh.vertexType, mesh.indices.size(), GL_UNSIGNED_INT, 0)
				}
				else {
					glDrawArrays(mesh.vertexType, 0, mesh.vertices.size())
				}

				trigger(new DrawEvent())
			}
		}
	}

	/**
	 * Enables the vertex attributes specified by the given vertex buffer layout
	 * object.
	 * 
	 * @param parts
	 * @return
	 */
	protected static void enableVertexBufferLayout(VertexBufferLayout layout) {

		def stride = layout.sizeInBytes()
		layout.parts.each { part ->
			glEnableVertexAttribArray(part.location)
			glVertexAttribPointer(part.location, part.size, GL_FLOAT, false, stride, layout.offsetOfInBytes(part))
		}
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

		def paletteLocation = getUniformLocation(standardPaletteShader, 'u_palette')
		glProgramUniform1i(standardPaletteShader.programId, paletteLocation, maxTextureUnits)
		glActiveTexture(GL_TEXTURE0 + maxTextureUnits)
		glBindTexture(GL_TEXTURE_1D, palette.textureId)
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
}
