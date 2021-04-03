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

package nz.net.ultraq.redhorizon.engine.graphics.opengl

import nz.net.ultraq.redhorizon.engine.graphics.BatchRenderer
import nz.net.ultraq.redhorizon.engine.graphics.Colour
import nz.net.ultraq.redhorizon.engine.graphics.DrawEvent
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.Material
import nz.net.ultraq.redhorizon.engine.graphics.Mesh
import nz.net.ultraq.redhorizon.engine.graphics.Shader
import nz.net.ultraq.redhorizon.engine.graphics.ShaderType
import nz.net.ultraq.redhorizon.engine.graphics.Texture
import nz.net.ultraq.redhorizon.engine.graphics.TextureCreatedEvent
import nz.net.ultraq.redhorizon.events.EventTarget
import nz.net.ultraq.redhorizon.geometry.Dimension

import static OpenGLRenderer.*

import org.joml.Matrix4f
import org.joml.Rectanglef
import org.joml.Vector2f
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.opengl.GL41C.*
import static org.lwjgl.system.MemoryStack.stackPush
import static org.lwjgl.system.MemoryUtil.NULL

import java.nio.ByteBuffer

/**
 * An extension to the {@code OpenGLRenderer} to support batch rendering.
 * 
 * @author Emanuel Rabina
 */
class OpenGLBatchRenderer implements GraphicsRenderer, BatchRenderer, EventTarget {

	private static final Logger logger = LoggerFactory.getLogger(OpenGLBatchRenderer)

	@Delegate(excludes = [
	  'createMaterial', 'createSpriteMesh', 'createTexture'
	])
	private final OpenGLRenderer renderer

	private final int maxTextureLayers = 512 // OpenGL 4.1 minimum seems to be 2048, which is plenty!
	private final int maxVertices
	private final int maxIndices

	// Batch buffers
	private final int batchVertexArrayId
	private final int batchVertexBufferId
	private final int batchElementBufferId
	private final int batchUniformBufferId

	// Batched data to be rendered on flush
	private List<Material> batchMaterials = []
	private List<Matrix4f> batchTransforms = []
	private int batchVertices = 0
	private int batchIndices = 0
	private int batchTextureUnit = 0
//	private Map<Dimensions,Integer> batchTextureLayers = [:]
//	private Map<Dimensions,Integer> batchTextureLayerUnits = [:]
//	private Map<Dimensions,Integer> batchTextureLayerIds = [:]
	private Map<Dimension,BatchTextureLayerInfo> batchTextureLayers = [:]

	private class BatchTextureLayerInfo {
		int textureId
		int textureUnit = -1
		int nextLayer
	}

	// Information about the current batch materials
	private Shader batchShader
	private int batchVertexType

	/**
	 * Create a new batch renderer that wraps an existing renderer, using it for
	 * most draw calls except those which need modification for batching.
	 * 
	 * @param renderer
	 */
	OpenGLBatchRenderer(OpenGLRenderer renderer) {

		this.renderer = renderer

		// Set up batch limits
//		maxTextureLayers = glGetInteger(GL_MAX_ARRAY_TEXTURE_LAYERS)
		def maxQuads = renderer.maxTransforms
		maxVertices = maxQuads * 4
		maxIndices = maxQuads * 6

		// Set up and bind the buffers used for batching our geometry
		batchVertexArrayId = glGenVertexArrays()
		glBindVertexArray(batchVertexArrayId)

		batchVertexBufferId = glGenBuffers()
		glBindBuffer(GL_ARRAY_BUFFER, batchVertexBufferId)
		glBufferData(GL_ARRAY_BUFFER, maxVertices * VERTEX_BUFFER_LAYOUT.sizeInBytes(), GL_DYNAMIC_DRAW)

		enableVertexBufferLayout(VERTEX_BUFFER_LAYOUT)

		batchElementBufferId = glGenBuffers()
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, batchElementBufferId)
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, maxIndices * Integer.BYTES, GL_DYNAMIC_DRAW)

		batchUniformBufferId = glGenBuffers()
		glBindBuffer(GL_UNIFORM_BUFFER, batchUniformBufferId)
		glBufferData(GL_UNIFORM_BUFFER, renderer.maxTransforms * Matrix4f.BYTES, GL_DYNAMIC_DRAW)
		renderer.shaders.eachWithIndex { shader, index ->
			def blockIndex = glGetUniformBlockIndex(shader.programId, 'Transforms')
			glUniformBlockBinding(shader.programId, blockIndex, 1)
		}
		glBindBufferBase(GL_UNIFORM_BUFFER, 1, batchUniformBufferId)
	}

	@Override
	void close() {

		glDeleteVertexArrays(batchVertexArrayId)
		glDeleteBuffers(batchVertexBufferId, batchElementBufferId, batchUniformBufferId)
		glDeleteTextures(batchTextureLayers.collect { entry -> entry.value.textureId } as int[])
	}

	@Override
	Mesh createLineLoopMesh(Colour colour, Vector2f... vertices) {

		return renderer.createMesh(GL_LINE_LOOP, colour, vertices)
	}

	@Override
	Mesh createLinesMesh(Colour colour, Vector2f... vertices) {

		return renderer.createMesh(GL_LINES, colour, vertices)
	}

	@Override
	Material createMaterial(Mesh mesh, Texture texture = renderer.whiteTexture, ShaderType shaderType = ShaderType.STANDARD) {

		return renderer.createMaterial(mesh, texture, shaderType)
	}

	@Override
	Mesh createSpriteMesh(Rectanglef surface, float repeatX = 1, float repeatY = 1) {

		return renderer.createMesh(
			GL_TRIANGLES,
			Colour.WHITE,
			surface as Vector2f[],
			new Rectanglef(0, 0, repeatX, repeatY) as Vector2f[],
			new int[]{ 0, 1, 3, 1, 2, 3 }
		)
	}

	@Override
	Texture createTexture(ByteBuffer data, int format, int width, int height, boolean filter = renderer.config.filter) {

		def texture = new Texture(
			dimensions: new Dimension(width, height),
			format: format,
			data: data
		)
		trigger(new TextureCreatedEvent(texture))
		return texture
	}

	@Override
	void drawMaterial(Material material, Matrix4f transform) {

		def mesh = material.mesh
		def texture = material.texture
		def shader = material.shader
		def dimensions = texture?.dimensions
		def textureLayerInfo = texture ? batchTextureLayers.getOrCreate(dimensions) { ->
			return new BatchTextureLayerInfo()
		} : null

		// If there is a change in shader, vertex information, or there is no space
		// for the next material, flush the current buffers
		if (
			shader != batchShader ||
			mesh.vertexType != batchVertexType ||
			((maxVertices - batchVertices < mesh.vertices.size())) ||
			((maxIndices - batchIndices) < mesh.indices.size()) ||
			(texture && (
				(maxTextureLayers - textureLayerInfo.nextLayer < 1) ||
				(renderer.maxTextureUnits - batchTextureUnit < 1)
			))
		) {
			flush()
		}

		if (shader != batchShader) {
			glUseProgram(shader.programId)
		}
		batchShader = shader
		batchVertexType = mesh.vertexType
		batchMaterials << material
		batchTransforms << transform
		batchVertices += mesh.vertices.size()
		batchIndices += mesh.indices.size()

		// TODO: See if the texture is being used in a previous material and so
		//       set the active texture target to that one
		if (texture) {
			// Set up new texture storage for the layer
			if (!textureLayerInfo.textureId) {
				textureLayerInfo.textureId = glGenTextures()
				glBindTexture(GL_TEXTURE_2D_ARRAY, textureLayerInfo.textureId)
				glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
				glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
				def format =
					texture.format == 1 ? GL_RED :
					texture.format == 3 ? GL_RGB :
					texture.format == 4 ? GL_RGBA :
					0
				glTexImage3D(GL_TEXTURE_2D_ARRAY, 0, format, dimensions.width, dimensions.height, maxTextureLayers, 0, format, GL_UNSIGNED_BYTE, NULL)
			}
			if (textureLayerInfo.textureUnit == -1) {
				textureLayerInfo.textureUnit = batchTextureUnit++
			}
			texture.layer = textureLayerInfo.nextLayer++
		}
	}

	@Override
	void flush() {

		averageNanos('flush', 1f, logger) { ->

			// Nothing to do
			if (!batchMaterials) {
				return
			}

			stackPush().withCloseable { stack ->

				// Build the sampler and model arrays for all of the materials in the
				// batch buffer, fill the vertex and index buffers with data from each
				// material
				def samplers = []
				def modelsBuffer = stack.mallocFloat(batchTransforms.size() * Matrix4f.FLOATS)
				def vertexBuffer = stack.mallocFloat(VERTEX_BUFFER_LAYOUT.size() * batchVertices)
				def indexBuffer = stack.mallocInt(batchIndices)
				def indexOffset = 0
				batchMaterials.eachWithIndex { material, materialIndex ->
					def texture = material.texture
					def dimensions = texture.dimensions
					def textureLayerInfo = batchTextureLayers[dimensions]
					def activeTexture = GL_TEXTURE0 + textureLayerInfo.textureUnit
					def currentActiveTexture = glGetInteger(GL_ACTIVE_TEXTURE)
					if (currentActiveTexture != activeTexture) {
						glActiveTexture(activeTexture)
						glBindTexture(GL_TEXTURE_2D_ARRAY, textureLayerInfo.textureId)
					}
					def format =
						texture.format == 1 ? GL_RED :
						texture.format == 3 ? GL_RGB :
						texture.format == 4 ? GL_RGBA :
						0
					// TODO: It's probably not efficient to always be uploading the image
					//       data to the GPU every frame right?
					def textureBuffer = stack.malloc(texture.data.remaining())
						.put(texture.data.array(), 0, texture.data.remaining())
						.flip()
					def matchesAlignment = (dimensions.width * texture.format) % 4 == 0
					if (!matchesAlignment) {
						checkForError { -> glPixelStorei(GL_UNPACK_ALIGNMENT, 1) }
					}
					glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0, 0, 0, texture.layer, dimensions.width, dimensions.height, 1, format, GL_UNSIGNED_BYTE, textureBuffer)
					if (!matchesAlignment) {
						checkForError { -> glPixelStorei(GL_UNPACK_ALIGNMENT, 4) }
					}

					if (!samplers.contains(textureLayerInfo.textureUnit)) {
						samplers << textureLayerInfo.textureUnit
					}

					batchTransforms[materialIndex].get(materialIndex * Matrix4f.FLOATS, modelsBuffer)

					def mesh = material.mesh
					def colour = mesh.colour
					def textureCoordinates = mesh.textureCoordinates
					mesh.vertices.eachWithIndex { vertex, vertexIndex ->
						def textureCoordinate = textureCoordinates[vertexIndex]
						vertexBuffer.put(
							colour.r, colour.g, colour.b, colour.a,
							vertex.x, vertex.y,
							textureCoordinate.x, textureCoordinate.y,
							textureLayerInfo.textureUnit,
							texture.layer,
							materialIndex
						)
					}
					mesh.indices.each { index ->
						indexBuffer.put(index + indexOffset)
					}
					indexOffset += mesh.vertices.size()
				}
				vertexBuffer.flip()
				indexBuffer.flip()

				// Pass all the built data to OpenGL
				def texturesLocation = getUniformLocation(batchShader, 'u_textures')
				glUniform1iv(texturesLocation, samplers as int[])
//				def modelsLocation = getUniformLocation(batchShader, 'models')
//				glUniformMatrix4fv(modelsLocation, false, models)
				glBindBuffer(GL_UNIFORM_BUFFER, batchUniformBufferId)
				glBufferSubData(GL_UNIFORM_BUFFER, 0, modelsBuffer)
				glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuffer)
				glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, 0, indexBuffer)

				// Draw it!
				if (indexBuffer.capacity()) {
					glDrawElements(batchVertexType, batchIndices, GL_UNSIGNED_INT, 0)
				}
				else {
					glDrawArrays(batchVertexType, 0, batchVertices)
				}
				trigger(new DrawEvent())

				// Reset batch tracking variables
				batchVertices = 0
				batchIndices = 0
				batchTextureUnit = 0
				batchTextureLayers.each { dimensions, textureLayerInfo ->
					textureLayerInfo.textureUnit = -1
					textureLayerInfo.nextLayer = 0
				}
				batchMaterials.clear()
				batchTransforms.clear()
			}
		}
	}
}
