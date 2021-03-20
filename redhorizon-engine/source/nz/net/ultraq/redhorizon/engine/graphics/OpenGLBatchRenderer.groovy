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
import static nz.net.ultraq.redhorizon.engine.graphics.OpenGLRenderer.*

import org.joml.Matrix4f
import org.joml.Rectanglef
import org.joml.Vector2f
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.opengl.GL41C.*
import static org.lwjgl.system.MemoryStack.stackPush

import java.nio.ByteBuffer

/**
 * An extension to the {@code OpenGLRenderer} to support batch rendering.
 * 
 * @author Emanuel Rabina
 */
class OpenGLBatchRenderer implements GraphicsRenderer, BatchRenderer, EventTarget {

	private static final Logger logger = LoggerFactory.getLogger(OpenGLBatchRenderer)

	private static final VertexBufferLayout VERTEX_BUFFER_LAYOUT = new VertexBufferLayout(
		VertexBufferLayoutParts.COLOUR,
		VertexBufferLayoutParts.POSITION,
		VertexBufferLayoutParts.TEXCOORD,
		VertexBufferLayoutParts.TEXUNIT
	)
	private static final int MAX_QUADS = 16 // TODO: Batch renderer is currently limited to drawing quads 😅
	private static final int MAX_VERTICES = MAX_QUADS * 4
	private static final int MAX_INDICES = MAX_QUADS * 6

	Shader shader
	Matrix4f modelMatrix

	@Delegate(excludes = [
	  'createSpriteMesh', 'createTexture'
	])
	private final OpenGLRenderer renderer

	private final int maxTextureUnits
	private final int batchVertexArrayId
	private final int batchVertexBufferId
	private final int batchElementBufferId
	private List<Material> batchMaterials = []
	private int batchVertices = 0
	private int batchIndices = 0
	private int batchTextureUnit = 0

	/**
	 * Create a new batch renderer that wraps an existing renderer, using it for
	 * most draw calls except those which need modification for batching.
	 * 
	 * @param renderer
	 */
	OpenGLBatchRenderer(OpenGLRenderer renderer) {

		this.renderer = renderer

		// Set up batch limits
		maxTextureUnits = glGetInteger(GL_MAX_TEXTURE_IMAGE_UNITS) - 1 // Last slot reserved for palette

		// Set up the buffers used for batching our geometry
		batchVertexArrayId = glGenVertexArrays()
		glBindVertexArray(batchVertexArrayId)

		batchVertexBufferId = glGenBuffers()
		glBindBuffer(GL_ARRAY_BUFFER, batchVertexBufferId)
		glBufferData(GL_ARRAY_BUFFER, MAX_VERTICES * VERTEX_BUFFER_LAYOUT.sizeInBytes(), GL_DYNAMIC_DRAW)

		setVertexBufferLayout(
			VertexBufferLayoutParts.COLOUR,
			VertexBufferLayoutParts.POSITION,
			VertexBufferLayoutParts.TEXCOORD,
			VertexBufferLayoutParts.TEXUNIT
		)

		// Generate an index buffer that repeats the pattern needed to draw quads up
		// to the size of the buffer
		batchElementBufferId = glGenBuffers()
		stackPush().withCloseable { stack ->
			def indexBuffer = stack.mallocInt(MAX_INDICES)
			for (def i = 0; i < MAX_INDICES / 6; i++) {
				def j = i * 4
				indexBuffer.put(j + 0, j + 1, j + 3, j + 1, j + 2, j + 3)
			}
			indexBuffer.flip()
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, batchElementBufferId)
			glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW)
		}
	}

	@Override
	void close() {

		glDeleteVertexArrays(batchVertexArrayId)
		glDeleteBuffers(batchVertexBufferId, batchElementBufferId)
		renderer.close()
	}

	@Override
	Mesh createLineLoopMesh(Colour colour, Vector2f... vertices) {

		return renderer.createMesh(colour, vertices)
	}

	@Override
	Mesh createLinesMesh(Colour colour, Vector2f... vertices) {

		return renderer.createMesh(colour, vertices)
	}

	@Override
	Mesh createSpriteMesh(Rectanglef surface, float repeatX = 1, float repeatY = 1) {

		return renderer.createMesh(
			Colour.WHITE,
			surface as Vector2f[],
			new Rectanglef(0, 0, repeatX, repeatY) as Vector2f[],
			[0, 1, 3, 1, 2, 3] as int[]
		)
	}

	@Override
	Texture createTexture(ByteBuffer data, int format, int width, int height, boolean filter = renderer.config.filter) {

		return renderer.createTexture(data, format, width, height, filter)
	}

	@Override
	void drawMaterial(Material material) {

		averageNanos('drawMaterial', 1f, logger) { ->
			def mesh = material.mesh
			def texture = material.texture

			// If there is no space for the next material, flush the current buffers
			if (!(mesh.vertices.size() <= (MAX_VERTICES - batchVertices)) ||
				!(mesh.indices.size() <= (MAX_INDICES - batchIndices)) ||
				!(texture && (batchTextureUnit < maxTextureUnits))) {
				flush()
			}

			// TODO: See if the texture is being used in a previous material and so
			//       set the active texture target to that one
			batchMaterials << material
			batchVertices += mesh.vertices.size()
			batchIndices += mesh.indices.size()
			if (texture) {
				batchTextureUnit++
			}
		}
	}

	@Override
	void flush() {

		// Nothing to do
		if (!batchMaterials) {
			return
		}

		stackPush().withCloseable { stack ->
			glUseProgram(shader.programId)

			// Bind each texture to all the available texture slots and build the
			// sampler uniform that will map to the textures
			def samplers = new int[maxTextureUnits]
			batchMaterials.eachWithIndex { material, index ->
				def texture = material.texture
				texture.textureUnit = index
				glActiveTexture(GL_TEXTURE0 + index)
				glBindTexture(GL_TEXTURE_2D, texture.textureId)
				samplers[index] = index
			}
			def textureLocation = getUniformLocation(shader, 'u_textures')
			glUniform1iv(textureLocation, samplers)

			def modelBuffer = modelMatrix.get(stack.mallocFloat(Matrix4f.FLOATS))
			def modelLocation = getUniformLocation(shader, 'model')
			glUniformMatrix4fv(modelLocation, false, modelBuffer)

			// Fill the vertex buffer with vertex data from the material
			def vertexBuffer = stack.mallocFloat(VERTEX_BUFFER_LAYOUT.size() * batchVertices)
			batchMaterials.each { material ->
				def mesh = material.mesh
				def colour = mesh.colour
				def textureCoordinates = mesh.textureCoordinates
				def texture = material.texture
				mesh.vertices.eachWithIndex { vertex, index ->
					def textureCoordinate = textureCoordinates[index]
					vertexBuffer.put(
						colour.r, colour.g, colour.b, colour.a,
						vertex.x, vertex.y,
						textureCoordinate.x, textureCoordinate.y,
						texture.textureUnit
					)
				}
			}
			vertexBuffer.flip()
			glBindBuffer(GL_ARRAY_BUFFER, batchVertexBufferId)
			glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuffer)

			glBindVertexArray(batchVertexArrayId)
			glDrawElements(GL_TRIANGLES, batchIndices, GL_UNSIGNED_INT, 0)
			trigger(drawEvent)

			batchVertices = 0
			batchIndices = 0
			batchTextureUnit = 0
			batchMaterials.clear()
		}
	}
}
