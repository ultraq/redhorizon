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
import nz.net.ultraq.redhorizon.events.EventTarget

import org.joml.Rectanglef
import static OpenGLRenderer.*

import org.joml.Matrix4f
import org.joml.Vector2f
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.opengl.GL41C.*
import static org.lwjgl.system.MemoryStack.stackPush

/**
 * An extension to the {@code OpenGLRenderer} to support batch rendering.
 * 
 * @author Emanuel Rabina
 */
class OpenGLBatchRenderer implements GraphicsRenderer<OpenGLFramebuffer, OpenGLMaterial, OpenGLMesh, OpenGLShader, OpenGLTexture>,
	BatchRenderer<OpenGLFramebuffer, OpenGLMaterial, OpenGLMesh, OpenGLShader, OpenGLTexture>, EventTarget {

	private static final Logger logger = LoggerFactory.getLogger(OpenGLBatchRenderer)

	@Delegate
	private final OpenGLRenderer renderer

	private final int maxQuads
	private final int maxVertices
	private final int maxIndices

	// Batch buffers
	private final int batchVertexArrayId
	private final int batchVertexBufferId
	private final int batchElementBufferId

	// Batched data to be rendered on flush
	private List<OpenGLMaterial> batchMaterials = []
	private int batchVertices = 0
	private int batchIndices = 0
	private int batchTextureUnit = 0
	private Map<OpenGLTexture,Integer> batchTextures = [:]

	// Information about the current batch materials
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
		maxQuads = renderer.maxTransforms
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
	}

	@Override
	void close() {

		glDeleteVertexArrays(batchVertexArrayId)
		glDeleteBuffers(batchVertexBufferId, batchElementBufferId)
	}

	@Override
	OpenGLMesh createLineLoopMesh(Colour colour, Vector2f... vertices) {

		return renderer.createMesh(GL_LINE_LOOP, colour, vertices)
	}

	@Override
	OpenGLMesh createLinesMesh(Colour colour, Vector2f... vertices) {

		return renderer.createMesh(GL_LINES, colour, vertices)
	}

	@Override
	OpenGLMesh createSpriteMesh(Rectanglef surface, Rectanglef textureUVs) {

		return renderer.createMesh(
			GL_TRIANGLES,
			Colour.WHITE,
			surface as Vector2f[],
			textureUVs as Vector2f[],
			new int[]{ 0, 1, 3, 1, 2, 3 }
		)
	}

	@Override
	void drawMaterial(OpenGLMaterial material) {

		def mesh = material.mesh
		def texture = material.texture

		// If there is a change in vertex information, or there is no space for the
		// next material, flush the current buffers
		if (
			mesh.vertexType != batchVertexType ||
			((maxVertices - batchVertices < mesh.vertices.size())) ||
			((maxIndices - batchIndices) < mesh.indices.size()) ||
			(texture && (renderer.maxTextureUnits - batchTextures.size() < 1))
		) {
			flush()
		}

		batchVertexType = mesh.vertexType
		batchMaterials << material
		batchVertices += mesh.vertices.size()
		batchIndices += mesh.indices.size()
		if (texture) {
			batchTextures.putIfAbsent(texture, batchTextureUnit++)
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
				glUseProgram(standardShader.programId)
				glBindVertexArray(batchVertexArrayId)

				// Build the sampler and model arrays for all of the materials in the
				// batch buffer, fill the vertex and index buffers with data from each
				// material
				def samplers = batchTextures.values() as int[]
				def models = stack.mallocFloat(batchMaterials.size() * Matrix4f.FLOATS)
				def vertexBuffer = stack.mallocFloat(VERTEX_BUFFER_LAYOUT.size() * batchVertices)
				def indexBuffer = stack.mallocInt(batchIndices)
				def indexOffset = 0
				def lastActiveTextureUnit = -1
				batchMaterials.eachWithIndex { material, materialIndex ->
					def texture = material.texture
					def textureUnit = batchTextures[texture]
					def palette = material.palette

					if (palette && palette != renderer.currentPalette) {
						def paletteLocation = getUniformLocation(renderer.standardShader, 'palette')
						glUniform1i(paletteLocation, renderer.maxTextureUnits)
						glActiveTexture(GL_TEXTURE0 + renderer.maxTextureUnits)
						glBindTexture(GL_TEXTURE_1D, palette.textureId)
						renderer.currentPalette = palette
					}
					def usePaletteLocation = getUniformLocation(renderer.standardShader, 'usePalette')
					glUniform1i(usePaletteLocation, palette ? 1 : 0)

					if (texture && (lastActiveTextureUnit == -1 || lastActiveTextureUnit != textureUnit)) {
						lastActiveTextureUnit = textureUnit
						glActiveTexture(GL_TEXTURE0 + textureUnit)
						glBindTexture(GL_TEXTURE_2D, texture.textureId)
					}

					material.transform.get(materialIndex * Matrix4f.FLOATS, models)

					def mesh = material.mesh
					def colour = mesh.colour
					def textureCoordinates = mesh.textureUVs
					mesh.vertices.eachWithIndex { vertex, vertexIndex ->
						def textureCoordinate = textureCoordinates[vertexIndex]
						vertexBuffer.put(
							colour.r, colour.g, colour.b, colour.a,
							vertex.x, vertex.y,
							textureCoordinate.x, textureCoordinate.y,
							textureUnit as float,
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
				def textureLocation = getUniformLocation(renderer.standardShader, 'textures')
				glUniform1iv(textureLocation, samplers)
				def modelsLocation = getUniformLocation(renderer.standardShader, 'models')
				glUniformMatrix4fv(modelsLocation, false, models)
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
				batchMaterials.clear()
				batchVertices = 0
				batchIndices = 0
				batchTextureUnit = 0
				batchTextures.clear()
			}
		}
	}
}
