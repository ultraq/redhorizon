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

import nz.net.ultraq.redhorizon.engine.geometry.Dimension
import nz.net.ultraq.redhorizon.engine.graphics.Colour
import nz.net.ultraq.redhorizon.engine.graphics.DrawEvent
import nz.net.ultraq.redhorizon.engine.graphics.Framebuffer
import nz.net.ultraq.redhorizon.engine.graphics.FramebufferCreatedEvent
import nz.net.ultraq.redhorizon.engine.graphics.FramebufferDeletedEvent
import nz.net.ultraq.redhorizon.engine.graphics.FramebufferSizeEvent
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.Material
import nz.net.ultraq.redhorizon.engine.graphics.Mesh
import nz.net.ultraq.redhorizon.engine.graphics.MeshCreatedEvent
import nz.net.ultraq.redhorizon.engine.graphics.MeshDeletedEvent
import nz.net.ultraq.redhorizon.engine.graphics.RendererEvent
import nz.net.ultraq.redhorizon.engine.graphics.Shader
import nz.net.ultraq.redhorizon.engine.graphics.ShaderUniformConfig
import nz.net.ultraq.redhorizon.engine.graphics.Texture
import nz.net.ultraq.redhorizon.engine.graphics.TextureCreatedEvent
import nz.net.ultraq.redhorizon.engine.graphics.TextureDeletedEvent
import nz.net.ultraq.redhorizon.engine.graphics.Uniform
import nz.net.ultraq.redhorizon.events.EventTarget

import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.primitives.Rectanglef
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GLCapabilities
import org.lwjgl.opengl.GLDebugMessageCallback
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.opengl.GL41C.*
import static org.lwjgl.opengl.KHRDebug.*
import static org.lwjgl.system.MemoryStack.stackPush
import static org.lwjgl.system.MemoryUtil.NULL

import groovy.transform.NamedParam
import groovy.transform.NamedVariant
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

	protected final GraphicsConfiguration config
	protected final GLCapabilities capabilities

	private Dimension framebufferSize
	private final Shader standardShader
	protected final List<Shader> shaders = []
	protected List<Integer> paletteTextureIds = []
	protected int cameraBufferObject

	/**
	 * Constructor, create a modern OpenGL renderer with a set of defaults for Red
	 * Horizon's 2D game engine.
	 * 
	 * @param config
	 * @param context
	 */
	OpenGLRenderer(GraphicsConfiguration config, OpenGLContext context) {

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

		// Track framebuffer size changes
		framebufferSize = context.framebufferSize
		context.on(FramebufferSizeEvent) { event ->
			framebufferSize = event.framebufferSize
		}

		// Create the shader programs used by this renderer
		standardShader = createShader('Standard',
			new Uniform('mainTexture') {
				@Override
				void apply(Material material, ShaderUniformConfig shaderConfig) {
					shaderConfig.setUniformTexture(name, 0, material.texture.textureId)
				}
			},
			new Uniform('model') {
				@Override
				void apply(Material material, ShaderUniformConfig shaderConfig) {
					shaderConfig.setUniformMatrix(name, material.transform)
				}
			}
		)
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
	Framebuffer createFramebuffer(Dimension resolution, boolean filter) {

		def frameBufferId = glGenFramebuffers()
		glBindFramebuffer(GL_FRAMEBUFFER, frameBufferId)

		def width = resolution.width
		def height = resolution.height

		// Colour texture attachment
		def colourTextureId = glGenTextures()
		glBindTexture(GL_TEXTURE_2D, colourTextureId)
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filter ? GL_LINEAR : GL_NEAREST)
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filter ? GL_LINEAR : GL_NEAREST)
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, NULL)
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, colourTextureId, 0)
		glBindTexture(GL_TEXTURE_2D, 0)

		def colourTexture = new Texture(
			textureId: colourTextureId,
			width: width,
			height: height
		)
		trigger(new TextureCreatedEvent(colourTexture))

		// Depth buffer attachment
		def depthBuffer = glGenRenderbuffers()
		glBindRenderbuffer(GL_RENDERBUFFER, depthBuffer)
		glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, width, height)
		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, depthBuffer)
		glBindRenderbuffer(GL_RENDERBUFFER, 0)

		glBindFramebuffer(GL_FRAMEBUFFER, 0)

		def framebuffer = new Framebuffer(
			framebufferId: frameBufferId,
			texture: colourTexture
		)
		trigger(new FramebufferCreatedEvent(framebuffer))
		return framebuffer
	}

	@Override
	Mesh createLineLoopMesh(Colour colour, Vector2f... vertices) {

		def mesh = new Mesh(
			vertexType: GL_LINE_LOOP,
			colour: colour,
			vertices: vertices
		)
		trigger(new MeshCreatedEvent(mesh))

		return createMeshData(mesh, new VertexBufferLayout(VertexBufferLayoutPart.COLOUR, VertexBufferLayoutPart.POSITION))
	}

	@Override
	Mesh createLinesMesh(Colour colour, Vector2f... vertices) {

		def mesh = new Mesh(
			vertexType: GL_LINES,
			colour: colour,
			vertices: vertices
		)
		trigger(new MeshCreatedEvent(mesh))

		return createMeshData(mesh, new VertexBufferLayout(VertexBufferLayoutPart.COLOUR, VertexBufferLayoutPart.POSITION))
	}

	@NamedVariant
	@Override
	Material createMaterial(@NamedParam(required = true) Mesh mesh, @NamedParam Texture texture,
		@NamedParam Shader shader, @NamedParam Matrix4f transform) {

		return new Material(
			mesh: mesh,
			texture: texture,
			shader: shader ?: standardShader,
			transform: transform ?: new Matrix4f()
		)
	}

	/**
	 * Create buffers based on the given mesh data representing some kind of
	 * OpenGL primitive.
	 * 
	 * @param mesh
	 * @param vertexBufferLayout
	 * @return
	 */
	private static Mesh createMeshData(Mesh mesh, VertexBufferLayout vertexBufferLayout) {

		return stackPush().withCloseable { stack ->
			def vertexArrayId = glGenVertexArrays()
			glBindVertexArray(vertexArrayId)

			def vertexBufferId = glGenBuffers()
			glBindBuffer(GL_ARRAY_BUFFER, vertexBufferId)

			// Buffer to hold all the vertex data
			def colour = mesh.colour
			def vertices = mesh.vertices
			def textureUVs = mesh.textureUVs
			def vertexBuffer = stack.mallocFloat(vertexBufferLayout.size() * vertices.size())
			vertices.eachWithIndex { vertex, index ->
				vertexBuffer.put(
					colour.r, colour.g, colour.b, colour.a,
					vertex.x, vertex.y
				)
				if (vertexBufferLayout.parts.contains(VertexBufferLayoutPart.TEXTURE_UVS)) {
					def textureUV = textureUVs[index]
					vertexBuffer.put(textureUV.x, textureUV.y)
				}
			}
			vertexBuffer.flip()
			glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW)

			enableVertexBufferLayout(vertexBufferLayout)

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

	@Override
	Shader createShader(String name, Uniform ...uniforms) {

		/* 
		 * Create a shader of the specified name and type, running a compilation
		 * check to make sure it all went OK.
		 */
		def createShader = { int type ->
			def shaderPath = "nz/net/ultraq/redhorizon/engine/graphics/opengl/${name}.${type == GL_VERTEX_SHADER ? 'vert' : 'frag'}.glsl"
			def shaderSource = getResourceAsStream(shaderPath).withBufferedStream { stream -> stream.text }
			def shaderId = glCreateShader(type)
			glShaderSource(shaderId, shaderSource)
			glCompileShader(shaderId)

			def status = glGetShaderi(shaderId, GL_COMPILE_STATUS)
			if (status != GL_TRUE) {
				var message = glGetShaderInfoLog(shaderId)
				logger.error(message)
				throw new Exception(message)
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
				var message = glGetProgramInfoLog(programId)
				logger.error(message)
				throw new Exception(message)
			}

			return programId
		}

		def shader = shaders.find { shader -> shader.name == name }
		if (!shader) {
			def vertexShaderId = createShader(GL_VERTEX_SHADER)
			def fragmentShaderId = createShader(GL_FRAGMENT_SHADER)
			def programId = createProgram(vertexShaderId, fragmentShaderId)
			glDeleteShader(vertexShaderId)
			glDeleteShader(fragmentShaderId)

			shader = new OpenGLShader(
				programId: programId,
				name: name,
				uniforms: uniforms
			)
			shaders << shader
		}
		return shader
	}

	@Override
	Mesh createSpriteMesh(Rectanglef surface, Rectanglef textureUVs) {

		def mesh = new Mesh(
			vertexType: GL_TRIANGLES,
			colour: Colour.WHITE,
			vertices: surface as Vector2f[],
			textureUVs: textureUVs as Vector2f[],
			indices: [0, 1, 3, 1, 2, 3] as int[]
		)
		trigger(new MeshCreatedEvent(mesh))

		return createMeshData(mesh, new VertexBufferLayout(
			VertexBufferLayoutPart.COLOUR,
			VertexBufferLayoutPart.POSITION,
			VertexBufferLayoutPart.TEXTURE_UVS
		))
	}

	@Override
	Texture createTexture(int width, int height, int format, ByteBuffer data) {

		return stackPush().withCloseable { stack ->
			def textureId = glGenTextures()
			glBindTexture(GL_TEXTURE_2D, textureId)
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)

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
				glPixelStorei(GL_UNPACK_ALIGNMENT, 1)
			}
			glTexImage2D(GL_TEXTURE_2D, 0, colourFormat, width, height, 0, colourFormat, GL_UNSIGNED_BYTE, textureBuffer)
			if (!matchesAlignment) {
				glPixelStorei(GL_UNPACK_ALIGNMENT, 4)
			}

			def texture = new Texture(
				textureId: textureId,
				width: width,
				height: height
			)
			trigger(new TextureCreatedEvent(texture))
			return texture
		}
	}

	@Override
	void deleteFramebuffer(Framebuffer framebuffer) {

		glDeleteFramebuffers(framebuffer.framebufferId)
		trigger(new FramebufferDeletedEvent(framebuffer))
	}

	@Override
	void deleteMaterial(Material material) {

		deleteMesh(material.mesh)
		def texture = material.texture
		if (texture && !paletteTextureIds.contains(texture.textureId)) {
			deleteTexture(texture)
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
	void drawMaterial(Material material) {

		averageNanos('drawMaterial', 1f, logger) { ->
			def mesh = material.mesh
			def shader = material.shader

			glUseProgram(shader.programId)

			var shaderUniformConfig = shader.withShaderUniformConfig()
			shader.uniforms.each { uniform ->
				uniform.apply(material, shaderUniformConfig)
			}

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

	@Override
	void setRenderTarget(Framebuffer framebuffer) {

		if (framebuffer) {
			glBindFramebuffer(GL_FRAMEBUFFER, framebuffer.framebufferId)
			glViewport(0, 0, framebuffer.texture.width, framebuffer.texture.height)
		}
		else {
			glBindFramebuffer(GL_FRAMEBUFFER, 0)
			glViewport(0, 0, framebufferSize.width, framebufferSize.height)
		}
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
		@ClosureParams(value = SimpleType, options = 'nz.net.ultraq.redhorizon.engine.graphics.MaterialBundler')
		Closure closure) {

		def materialBuilder = new OpenGLMaterialBundler(this)
		materialBuilder.relay(RendererEvent, this)
		closure(materialBuilder)
		return materialBuilder.bundle()
	}
}
