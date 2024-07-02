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
import nz.net.ultraq.redhorizon.engine.graphics.Attribute
import nz.net.ultraq.redhorizon.engine.graphics.Colour
import nz.net.ultraq.redhorizon.engine.graphics.DrawEvent
import nz.net.ultraq.redhorizon.engine.graphics.Framebuffer
import nz.net.ultraq.redhorizon.engine.graphics.FramebufferCreatedEvent
import nz.net.ultraq.redhorizon.engine.graphics.FramebufferDeletedEvent
import nz.net.ultraq.redhorizon.engine.graphics.FramebufferSizeEvent
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsResource
import nz.net.ultraq.redhorizon.engine.graphics.Material
import nz.net.ultraq.redhorizon.engine.graphics.Mesh
import nz.net.ultraq.redhorizon.engine.graphics.MeshCreatedEvent
import nz.net.ultraq.redhorizon.engine.graphics.MeshDeletedEvent
import nz.net.ultraq.redhorizon.engine.graphics.MeshType
import nz.net.ultraq.redhorizon.engine.graphics.Shader
import nz.net.ultraq.redhorizon.engine.graphics.SpriteSheet
import nz.net.ultraq.redhorizon.engine.graphics.Texture
import nz.net.ultraq.redhorizon.engine.graphics.TextureCreatedEvent
import nz.net.ultraq.redhorizon.engine.graphics.TextureDeletedEvent
import nz.net.ultraq.redhorizon.engine.graphics.Uniform
import nz.net.ultraq.redhorizon.engine.graphics.UniformBuffer
import nz.net.ultraq.redhorizon.engine.graphics.UniformBufferCreatedEvent
import nz.net.ultraq.redhorizon.engine.graphics.UniformBufferDeletedEvent
import nz.net.ultraq.redhorizon.engine.graphics.VertexBufferLayout
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
import static org.lwjgl.opengl.GL20C.GL_MAX_FRAGMENT_UNIFORM_COMPONENTS
import static org.lwjgl.opengl.GL30C.GL_FRAMEBUFFER
import static org.lwjgl.opengl.GL30C.glBindFramebuffer
import static org.lwjgl.opengl.GL31C.*
import static org.lwjgl.opengl.KHRDebug.*

import java.lang.reflect.Modifier
import java.nio.Buffer
import java.nio.ByteBuffer

/**
 * A graphics renderer utilizing the modern OpenGL API, version 4.1.
 *
 * @author Emanuel Rabina
 */
class OpenGLRenderer implements GraphicsRenderer {

	private static final Logger logger = LoggerFactory.getLogger(OpenGLRenderer)

	protected final GraphicsConfiguration config
	protected final OpenGLWindow window
	protected final GLCapabilities capabilities

	private final String renderer
	private final String version
	private final int maxTextureSize
	private final int maxFragmentUniformComponents
	private final int maxUniformBufferBindings
	private Dimension framebufferSize
	private List<OpenGLShader> shaders = []
	private List<OpenGLUniformBuffer> uniformBuffers = []

	/**
	 * Constructor, create a modern OpenGL renderer with a set of defaults for Red
	 * Horizon's 2D game engine.
	 */
	OpenGLRenderer(GraphicsConfiguration config, OpenGLWindow window) {

		this.config = config
		this.window = window

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

		// Create the default shader programs used by this renderer
		createShader(new SpriteShader())
		createShader(new PrimitivesShader())

		// Some graphics hardware info we're interested in
		renderer = glGetString(GL_RENDERER)
		version = glGetString(GL_VERSION)
		maxTextureSize = glGetInteger(GL_MAX_TEXTURE_SIZE)
		maxFragmentUniformComponents = glGetInteger(GL_MAX_FRAGMENT_UNIFORM_COMPONENTS)
		maxUniformBufferBindings = glGetInteger(GL_MAX_UNIFORM_BUFFER_BINDINGS)
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

		// For an error w/ nVidia on Windows, where the program state seems to
		// linger when creating a shader with a different layout, eg: we last used
		// the Sharp Upscaling shader which has all of colour, position, and
		// texcoord attributes, then try to create the Primitives shader which has
		// only colour and position.  This then manifests as an error on the next
		// frame when we call glClear() 🤔
		// "Program/shader state performance warning: Vertex shader in program X
		// is being recompiled based on GL state"
		OpenGLShader.useProgram(0)

		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)
	}

	@Override
	void close() {

		shaders*.close()
	}

	@Override
	Framebuffer createFramebuffer(int width, int height, boolean filter) {

		// Colour texture attachment
		var colourTexture = new OpenGLTexture(width, height, filter)
		trigger(new TextureCreatedEvent(colourTexture))

		var framebuffer = new OpenGLFramebuffer(width, height, colourTexture)
		trigger(new FramebufferCreatedEvent(framebuffer))
		return framebuffer
	}

	@Override
	Mesh createMesh(MeshType type, VertexBufferLayout layout, Vector2f[] vertices, Colour colour, Vector2f[] textureUVs,
		int[] indices) {

		var mesh = new OpenGLMesh(type == MeshType.LINES ? GL_LINES : type == MeshType.LINE_LOOP ? GL_LINE_LOOP : GL_TRIANGLES,
			layout, vertices, colour, textureUVs, indices)
		trigger(new MeshCreatedEvent(mesh))
		return mesh
	}

	@Override
	Shader createShader(String name, String vertexShaderSource, String fragmentShaderSource, Attribute[] attributes, Uniform[] uniforms) {

		var shader = shaders.find { shader -> shader.name == name }
		if (!shader) {
			shader = new OpenGLShader(name, vertexShaderSource, fragmentShaderSource, attributes, uniforms)
			shaders << shader

			// Link uniform buffers with the shader if applicable
			uniformBuffers.each { uniformBuffer ->
				var blockIndex = glGetUniformBlockIndex(shader.programId, uniformBuffer.name)
				if (blockIndex != GL_INVALID_INDEX) {
					glUniformBlockBinding(shader.programId, blockIndex, uniformBuffer.blockIndex)
				}
			}
		}
		return shader
	}

	@Override
	Mesh createSpriteMesh(Rectanglef surface, Rectanglef textureUVs = new Rectanglef(0, 0, 1, 1)) {

		return createMesh(
			MeshType.TRIANGLES,
			new VertexBufferLayout(Attribute.POSITION, Attribute.COLOUR, Attribute.TEXTURE_UVS),
			surface as Vector2f[],
			Colour.WHITE,
			textureUVs as Vector2f[],
			0, 1, 3, 1, 2, 3
		)
	}

	@Override
	SpriteSheet createSpriteSheet(int width, int height, ColourFormat format, ByteBuffer[] data) {

		var framesHorizontal = Math.min(data.length, maxTextureSize / width as int)
		var framesVertical = Math.ceil(data.length / framesHorizontal) as int
		var spriteSheetWidth = framesHorizontal * width
		var spriteSheetHeight = framesVertical * height
		logger.trace('Sprite sheet - across: {}, down: {}, width: {}, height: {}',
			framesHorizontal, framesVertical, spriteSheetWidth, spriteSheetHeight)

		var frameStepX = 1 / framesHorizontal
		var frameStepY = 1 / framesVertical
		logger.trace('Texture UV steps for sprite sheet: {}x{}', frameStepX, frameStepY)

		var imagesAsSpriteSheet = data
			.flipVertical(width, height, format)
			.combineImages(width, height, format, framesHorizontal)
		var texture = createTexture(spriteSheetWidth, spriteSheetHeight, format, imagesAsSpriteSheet)

		return new SpriteSheet(texture, framesHorizontal, framesVertical, frameStepX, frameStepY)
	}

	@Override
	Texture createTexture(int width, int height, ColourFormat format, ByteBuffer data) {

		var texture = new OpenGLTexture(width, height, format, data)
		trigger(new TextureCreatedEvent(texture))
		return texture
	}

	@Override
	UniformBuffer createUniformBuffer(String name, Buffer data, boolean global) {

		var uniformBuffer = new OpenGLUniformBuffer(name, data, global)
		uniformBuffers << uniformBuffer

		// Link this uniform buffer with all shaders made thus far
		shaders.each { shader ->
			var blockIndex = glGetUniformBlockIndex(shader.programId, uniformBuffer.name)
			if (blockIndex != GL_INVALID_INDEX) {
				glUniformBlockBinding(shader.programId, blockIndex, uniformBuffer.blockIndex)
			}
		}

		trigger(new UniformBufferCreatedEvent(uniformBuffer))
		return uniformBuffer
	}

	@Override
	void delete(GraphicsResource resource) {

		if (resource) {
			resource.close()
			switch (resource) {
				case Framebuffer -> {
					trigger(new TextureDeletedEvent(resource.texture))
					trigger(new FramebufferDeletedEvent(resource))
				}
				case Mesh -> trigger(new MeshDeletedEvent(resource))
				case Texture -> trigger(new TextureDeletedEvent(resource))
				case SpriteSheet -> trigger(new TextureDeletedEvent(resource.texture))
				case UniformBuffer -> trigger(new UniformBufferDeletedEvent(resource))
				default -> throw new IllegalArgumentException("Cannot delete resource of type ${resource}")
			}
		}
	}

	@Override
	void draw(Mesh mesh, Matrix4f transform, Shader shader, Material material = null) {

		averageNanos('draw', 1f, logger) { ->
			shader.use()
			shader.applyUniforms(transform, material, window)
			mesh.bind()
			if (mesh.indices) {
				glDrawElements(mesh.vertexType, mesh.indices.size(), GL_UNSIGNED_INT, 0)
			}
			else {
				glDrawArrays(mesh.vertexType, 0, mesh.vertices.size())
			}

			trigger(new DrawEvent())
		}
	}

	@Override
	void setRenderTarget(Framebuffer framebuffer) {

		if (framebuffer) {
			framebuffer.bind()
			glViewport(0, 0, framebuffer.texture.width, framebuffer.texture.height)
		}
		else {
			glBindFramebuffer(GL_FRAMEBUFFER, 0)
			glViewport(0, 0, framebufferSize.width(), framebufferSize.height())
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
			OpenGL device: ${renderer}, OpenGL ${version}
			 - Max texture size: ${maxTextureSize}
			 - Max fragment uniform components: ${maxFragmentUniformComponents}
			 - Max uniform buffer bindings: ${maxUniformBufferBindings}
		""".stripIndent().stripTrailing()
	}
}
