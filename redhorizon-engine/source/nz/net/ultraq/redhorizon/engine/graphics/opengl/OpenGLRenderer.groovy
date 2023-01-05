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
import nz.net.ultraq.redhorizon.engine.graphics.MeshType
import nz.net.ultraq.redhorizon.engine.graphics.RendererEvent
import nz.net.ultraq.redhorizon.engine.graphics.Shader
import nz.net.ultraq.redhorizon.engine.graphics.Texture
import nz.net.ultraq.redhorizon.engine.graphics.TextureCreatedEvent
import nz.net.ultraq.redhorizon.engine.graphics.TextureDeletedEvent
import nz.net.ultraq.redhorizon.engine.graphics.Uniform
import nz.net.ultraq.redhorizon.engine.graphics.VertexBufferLayout
import nz.net.ultraq.redhorizon.engine.graphics.VertexBufferLayoutPart
import nz.net.ultraq.redhorizon.events.EventTarget
import nz.net.ultraq.redhorizon.filetypes.ColourFormat

import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.primitives.Rectanglef
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL41C
import org.lwjgl.opengl.GLCapabilities
import org.lwjgl.opengl.GLDebugMessageCallback
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.opengl.GL11C.*
import static org.lwjgl.opengl.GL15C.*
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer
import static org.lwjgl.opengl.GL30C.*
import static org.lwjgl.opengl.KHRDebug.*
import static org.lwjgl.system.MemoryStack.stackPush

import groovy.transform.NamedVariant
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import java.lang.reflect.Modifier
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
	private final Shader spriteShader
	protected final List<Shader> shaders = []
	protected int cameraBufferObject

	/**
	 * Constructor, create a modern OpenGL renderer with a set of defaults for Red
	 * Horizon's 2D game engine.
	 *
	 * @param config
	 * @param window
	 */
	OpenGLRenderer(GraphicsConfiguration config, OpenGLWindow window) {

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
		framebufferSize = window.framebufferSize
		window.on(FramebufferSizeEvent) { event ->
			framebufferSize = event.framebufferSize
		}

		// Create the shader programs used by this renderer
		spriteShader = createShader(
			'Sprite',
			getResourceAsStream('nz/net/ultraq/redhorizon/engine/graphics/opengl/Sprite.vert.glsl').text,
			getResourceAsStream('nz/net/ultraq/redhorizon/engine/graphics/opengl/Sprite.frag.glsl').text,
			{ shader, material ->
				shader.setUniformTexture('mainTexture', 0, material.texture)
			},
			{ shader, material ->
				shader.setUniformMatrix('model', material.transform)
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
			var errorCode = GL41C.getFields().find { field ->
				return Modifier.isStatic(field.modifiers) && field.name.startsWith("GL_") && field.getInt(null) == error
			}
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

		glDeleteBuffers(cameraBufferObject)
		shaders*.close()
	}

	@Override
	Framebuffer createFramebuffer(Dimension resolution, boolean filter) {

		def frameBufferId = glGenFramebuffers()
		glBindFramebuffer(GL_FRAMEBUFFER, frameBufferId)

		def width = resolution.width
		def height = resolution.height

		// Colour texture attachment
		var colourTexture = new OpenGLTexture(width, height, filter)
		trigger(new TextureCreatedEvent(colourTexture))

		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, colourTexture.textureId, 0)

		// Depth buffer attachment
		def depthBufferId = glGenRenderbuffers()
		glBindRenderbuffer(GL_RENDERBUFFER, depthBufferId)
		glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, width, height)
		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, depthBufferId)
		glBindRenderbuffer(GL_RENDERBUFFER, 0)

		glBindFramebuffer(GL_FRAMEBUFFER, 0)

		def framebuffer = new Framebuffer(
			framebufferId: frameBufferId,
			texture: colourTexture,
			depthBufferId: depthBufferId
		)
		trigger(new FramebufferCreatedEvent(framebuffer))
		return framebuffer
	}

	@Override
	@NamedVariant
	Material createMaterial(Texture texture = null, Matrix4f transform = new Matrix4f()) {

		return new Material(
			texture: texture,
			transform: transform
		)
	}

	@Override
	@NamedVariant
	Mesh createMesh(MeshType type, VertexBufferLayout layout, Colour colour, Vector2f[] vertices,
		Vector2f[] textureUVs = null, int[] indices = null) {

		def mesh = new Mesh(
			vertexType: type == MeshType.LINES ? GL_LINES : type == MeshType.LINE_LOOP ? GL_LINE_LOOP : GL_TRIANGLES,
			colour: colour,
			vertices: vertices,
			textureUVs: textureUVs,
			indices: indices
		)
		trigger(new MeshCreatedEvent(mesh))

		return stackPush().withCloseable { stack ->
			def vertexArrayId = glGenVertexArrays()
			glBindVertexArray(vertexArrayId)

			def vertexBufferId = glGenBuffers()
			glBindBuffer(GL_ARRAY_BUFFER, vertexBufferId)

			// Buffer to hold all the vertex data
			def vertexBuffer = stack.mallocFloat(layout.size() * vertices.size())
			vertices.eachWithIndex { vertex, index ->
				vertexBuffer.put(
					colour.r, colour.g, colour.b, colour.a,
					vertex.x, vertex.y
				)
				if (layout.parts.contains(VertexBufferLayoutPart.TEXTURE_UVS)) {
					def textureUV = textureUVs[index]
					vertexBuffer.put(textureUV.x, textureUV.y)
				}
			}
			vertexBuffer.flip()
			glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW)

			enableVertexBufferLayout(layout)

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
	Shader createShader(String name, String vertexShaderSource, String fragmentShaderSource, Uniform... uniforms) {

		var shader = shaders.find { shader -> shader.name == name }
		if (!shader) {
			shader = new OpenGLShader(name, vertexShaderSource, fragmentShaderSource, uniforms)
			shaders << shader
		}
		return shader
	}

	@Override
	@NamedVariant
	Mesh createSpriteMesh(Rectanglef surface, Rectanglef textureUVs = new Rectanglef(0, 0, 1, 1)) {

		return createMesh(
			type: MeshType.TRIANGLES,
			layout: new VertexBufferLayout(
				VertexBufferLayoutPart.COLOUR,
				VertexBufferLayoutPart.POSITION,
				VertexBufferLayoutPart.TEXTURE_UVS
			),
			colour: Colour.WHITE,
			vertices: surface as Vector2f[],
			textureUVs: textureUVs as Vector2f[],
			indices: [0, 1, 3, 1, 2, 3] as int[]
		)
	}

	@Override
	Texture createTexture(int width, int height, ColourFormat format, ByteBuffer data) {

		var texture = new OpenGLTexture(width, height, format, data)
		trigger(new TextureCreatedEvent(texture))
		return texture
	}

	@Override
	void deleteFramebuffer(Framebuffer framebuffer) {

		if (framebuffer) {
			glDeleteFramebuffers(framebuffer.framebufferId)
			glDeleteRenderbuffers(framebuffer.depthBufferId)
			deleteTexture(framebuffer.texture)
			trigger(new FramebufferDeletedEvent(framebuffer))
		}
	}

	@Override
	void deleteMaterial(Material material) {

		if (material) {
			if (material.texture) {
				deleteTexture(material.texture)
			}
		}
	}

	@Override
	void deleteMesh(Mesh mesh) {

		if (mesh) {
			if (mesh.elementBufferId) {
				glDeleteBuffers(mesh.elementBufferId)
			}
			glDeleteBuffers(mesh.vertexBufferId)
			glDeleteVertexArrays(mesh.vertexArrayId)
			trigger(new MeshDeletedEvent(mesh))
		}
	}

	@Override
	void deleteTexture(Texture texture) {

		if (texture) {
			texture.close()
			trigger(new TextureDeletedEvent(texture))
		}
	}

	@Override
	@NamedVariant
	void draw(Mesh mesh, Shader shader = spriteShader, Material material = null) {

		averageNanos('draw', 1f, logger) { ->
			shader.use()
			if (material) {
				shader.applyMaterial(material)
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
	Tuple2<Mesh, Material> withMaterialBundler(
		@ClosureParams(value = SimpleType, options = 'nz.net.ultraq.redhorizon.engine.graphics.MaterialBundler')
			Closure closure) {

		def materialBuilder = new OpenGLMaterialBundler(this)
		materialBuilder.relay(RendererEvent, this)
		closure(materialBuilder)
		return materialBuilder.bundle()
	}
}
