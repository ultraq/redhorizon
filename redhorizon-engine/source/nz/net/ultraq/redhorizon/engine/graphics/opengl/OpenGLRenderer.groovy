/* 
 * Copyright 2019, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import nz.net.ultraq.redhorizon.engine.graphics.Colour
import nz.net.ultraq.redhorizon.engine.graphics.DrawEvent
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.Material
import nz.net.ultraq.redhorizon.engine.graphics.Mesh
import nz.net.ultraq.redhorizon.engine.graphics.MeshCreatedEvent
import nz.net.ultraq.redhorizon.engine.graphics.MeshDeletedEvent
import nz.net.ultraq.redhorizon.engine.graphics.RendererEvent
import nz.net.ultraq.redhorizon.engine.graphics.Texture
import nz.net.ultraq.redhorizon.engine.graphics.TextureCreatedEvent
import nz.net.ultraq.redhorizon.engine.graphics.TextureDeletedEvent
import nz.net.ultraq.redhorizon.events.EventTarget
import nz.net.ultraq.redhorizon.geometry.Dimension
import static nz.net.ultraq.redhorizon.filetypes.ColourFormat.*

import org.joml.Matrix4f
import org.joml.Rectanglef
import org.joml.Vector2f
import org.joml.Vector3f
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
		VertexBufferLayoutParts.TEXTURE_UVS,
		VertexBufferLayoutParts.TEXTURE_UNIT,
		VertexBufferLayoutParts.MODEL_INDEX
	)

	protected final GraphicsConfiguration config
	protected final GLCapabilities capabilities
	protected final int maxTextureUnits
	protected final int maxTransforms

	protected final Dimension viewportSize
	protected final OpenGLShader standardShader
	protected final List<OpenGLShader> shaders = []
	protected Texture whiteTexture
	protected List<Integer> paletteTextureIds = []
	protected int cameraBufferObject
	protected Texture currentPalette

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
		maxTextureUnits = glGetInteger(GL_MAX_TEXTURE_IMAGE_UNITS)
		maxTransforms = 32

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
		viewportSize = context.framebufferSize
		logger.debug('Establishing a viewport of size {}', viewportSize)
		glViewport(0, 0, viewportSize.width, viewportSize.height)
//		context.on(FramebufferSizeEvent) { event ->
//			logger.debug('Updating viewport to size {}x{}', event.width, event.height)
//			glViewport(0, 0, event.width, event.height)
//		}

		// Create the shader programs used by this renderer
		standardShader = createShader('Standard')

		// The white texture used as a fallback when no texture is bound
		stackPush().withCloseable { stack ->
			def textureBytes = ByteBuffer.allocateNative(4)
				.putInt(0xffffffff as int)
				.flip()
			whiteTexture = createTexture(textureBytes, FORMAT_RGBA.value, 1, 1)
		}
	}

	@Override
	void asBatchRenderer(
		@ClosureParams(value = SimpleType, options = 'nz.net.ultraq.redhorizon.engine.graphics.BatchRenderer')
		Closure closure) {

		if (!batchRenderer) {
			batchRenderer = new OpenGLBatchRenderer(this)
			batchRenderer.relay(RendererEvent, this)
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

		if (batchRenderer) {
			batchRenderer.close()
		}
		glDeleteTextures(*paletteTextureIds)
		glDeleteBuffers(cameraBufferObject)
		shaders.each { shader ->
			glDeleteProgram(shader.programId)
		}
	}

	@Override
	void createCamera(Matrix4f projection, Matrix4f view) {

		stackPush().withCloseable { stack ->
			cameraBufferObject = glGenBuffers()
			glBindBuffer(GL_UNIFORM_BUFFER, cameraBufferObject)
			def projectionAndViewBuffer = stack.mallocFloat(Matrix4f.FLOATS * 2)
			projection.get(0, projectionAndViewBuffer)
			view.get(Matrix4f.FLOATS, projectionAndViewBuffer)
			glBufferData(GL_UNIFORM_BUFFER, projectionAndViewBuffer, GL_DYNAMIC_DRAW)

			shaders.each { shader ->
				def blockIndex = glGetUniformBlockIndex(shader.programId, 'Camera')
				glUniformBlockBinding(shader.programId, blockIndex, 0)
			}
			glBindBufferBase(GL_UNIFORM_BUFFER, 0, cameraBufferObject)
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
	Material createMaterial(Mesh mesh, Texture texture, Matrix4f transform) {

		return new OpenGLMaterial(
			mesh: mesh,
			texture: texture ?: whiteTexture,
			shader: standardShader,
			transform: transform
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
	 * @param textureUVs
	 * @param indices
	 * @return
	 */
	protected OpenGLMesh createMesh(int vertexType, Colour colour, Vector2f[] vertices,
		Vector2f[] textureUVs = new Rectanglef() as Vector2f[], int[] indices = new int[0]) {

		def mesh = new OpenGLMesh(
			vertexType: vertexType,
			colour: colour,
			vertices: vertices,
			textureUVs: textureUVs,
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
	private static OpenGLMesh createMeshData(OpenGLMesh mesh) {

		return stackPush().withCloseable { stack ->
			def vertexArrayId = glGenVertexArrays()
			glBindVertexArray(vertexArrayId)

			def vertexBufferId = glGenBuffers()
			glBindBuffer(GL_ARRAY_BUFFER, vertexBufferId)

			// Buffer to hold all the vertex data
			def colour = mesh.colour
			def vertices = mesh.vertices
			def textureUVs = mesh.textureUVs
			def vertexBuffer = stack.mallocFloat(VERTEX_BUFFER_LAYOUT.size() * vertices.size())
			vertices.eachWithIndex { vertex, index ->
				def textureUV = textureUVs[index]
				vertexBuffer.put(
					colour.r, colour.g, colour.b, colour.a,
					vertex.x, vertex.y,
					textureUV.x, textureUV.y,
					0, 0
				)
			}
			vertexBuffer.flip()
			glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW)

			enableVertexBufferLayout(VERTEX_BUFFER_LAYOUT)

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
	private OpenGLShader createShader(String name) {

		/* 
		 * Create a shader of the specified name and type, running a compilation
		 * check to make sure it all went OK.
		 */
		def createShader = { int type, Closure<String> mod = null ->
			def shaderPath = "nz/net/ultraq/redhorizon/engine/graphics/opengl/${name}.${type == GL_VERTEX_SHADER ? 'vert' : 'frag'}.glsl"
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
			return source
				.replace('MAX_TEXTURE_UNITS', "${maxTextureUnits}")
				.replace('MAX_TRANSFORMS', "${maxTransforms}")
				.replace('OUTPUT_RESOLUTION_WIDTH', "${viewportSize.width}")
				.replace('OUTPUT_RESOLUTION_HEIGHT', "${viewportSize.height}")
		}
		def vertexShaderId = createShader(GL_VERTEX_SHADER, capTextureUnits)
		def fragmentShaderId = createShader(GL_FRAGMENT_SHADER, capTextureUnits)
		def programId = createProgram(vertexShaderId, fragmentShaderId)
		glDeleteShader(vertexShaderId)
		glDeleteShader(fragmentShaderId)

		def shader = new OpenGLShader(
			name: name,
			programId: programId
		)
		shaders << shader
		return shader
	}

	@Override
	Mesh createSpriteMesh(Rectanglef surface, Rectanglef textureUVs = new Rectanglef(0, 0, 1, 1)) {

		return createMeshData(createMesh(
			GL_TRIANGLES,
			Colour.WHITE,
			surface as Vector2f[],
			textureUVs as Vector2f[],
			new int[]{ 0, 1, 3, 1, 2, 3 }
		))
	}

	@Override
	Texture createTexture(ByteBuffer data, int format, int width, int height) {

		return stackPush().withCloseable { stack ->
			int textureId = glGenTextures()
			glBindTexture(GL_TEXTURE_2D, textureId)
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, config.filter ? GL_LINEAR : GL_NEAREST)
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, config.filter ? GL_LINEAR : GL_NEAREST)

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

			def texture = new OpenGLTexture(
				textureId: textureId,
				width: width,
				height: height
			)
			trigger(new TextureCreatedEvent(texture))
			return texture
		}
	}

	@Override
	void deleteMaterial(Material material) {

		deleteMesh(material.mesh)
		def texture = material.texture as OpenGLTexture
		if (texture && !paletteTextureIds.contains(texture.textureId)) {
			deleteTexture(material.texture)
		}
	}

	@Override
	void deleteMesh(Mesh mesh) {

		mesh = mesh as OpenGLMesh
		if (mesh.elementBufferId) {
			glDeleteBuffers(mesh.elementBufferId)
		}
		glDeleteBuffers(mesh.vertexBufferId)
		glDeleteVertexArrays(mesh.vertexArrayId)
		trigger(new MeshDeletedEvent(mesh))
	}

	@Override
	void deleteTexture(Texture texture) {

		texture = texture as OpenGLTexture
		glDeleteTextures(texture.textureId)
		trigger(new TextureDeletedEvent(texture))
	}

	@Override
	void drawMaterial(Material material) {

		averageNanos('drawMaterial', 1f, logger) { ->
			stackPush().withCloseable { stack ->
				def mesh = material.mesh as OpenGLMesh
				def texture = material.texture as OpenGLTexture
				def shader = material.shader as OpenGLShader

				glUseProgram(shader.programId)

				def texturesLocation = getUniformLocation(shader, 'textures')
				glUniform1iv(texturesLocation, 0)
				glActiveTexture(GL_TEXTURE0)
				glBindTexture(GL_TEXTURE_2D, texture.textureId)

				glUniform2fv(getUniformLocation(shader, 'textureSourceSize'), stack.floats(texture.width, texture.height))

				// TODO: Store mesh dimensions somewhere?  Currently just a series of
				//       points but no way to get the original size from the Rectanglef
				//       that it was built with 🤔
				def textureTargetSize = mesh.vertices[2].mul(material.transform.getScale(new Vector3f()).x, new Vector2f())
				glUniform2fv(getUniformLocation(shader, 'textureTargetSize'), textureTargetSize.get(stack.mallocFloat(Vector2f.FLOATS)))

				glUniform1i(getUniformLocation(shader, 'useScanlines'), material.scanlines ? 1 : 0)

				def modelsBuffer = material.transform.get(stack.mallocFloat(Matrix4f.FLOATS))
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
	protected static int getUniformLocation(OpenGLShader shader, String name) {

		return glGetUniformLocation(shader.programId, name)
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
			glBindBuffer(GL_UNIFORM_BUFFER, cameraBufferObject)
			glBufferSubData(GL_UNIFORM_BUFFER, Matrix4f.BYTES, view.get(stack.mallocFloat(Matrix4f.FLOATS)))
		}
	}

	@Override
	Material withMaterialBundler(
		@ClosureParams(value = SimpleType, options = 'nz.net.ultraq.redhorizon.engine.graphics.MaterialBuilder')
		Closure closure) {

		def materialBuilder = new OpenGLMaterialBundler(this)
		materialBuilder.relay(RendererEvent, this)
		closure(materialBuilder)
		return materialBuilder.bundle()
	}
}
