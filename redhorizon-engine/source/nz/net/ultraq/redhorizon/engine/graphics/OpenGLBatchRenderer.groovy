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

import groovy.transform.MapConstructor
import java.nio.ByteBuffer

/**
 * An extension to the {@code OpenGLRenderer} to support batch rendering.
 * 
 * @author Emanuel Rabina
 */
class OpenGLBatchRenderer implements GraphicsRenderer, BatchRenderer, EventTarget {

	private static final Logger logger = LoggerFactory.getLogger(OpenGLBatchRenderer)

	private static final int MAX_VERTICES = 8
	private static final int MAX_INDICES = 12

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
	 * A class describing the data that is stored in a vertex buffer for a single
	 * vertex.
	 *
	 * @author Emanuel Rabina
	 */
	@MapConstructor
	class Vertex {

		static final VertexBufferLayout LAYOUT = new VertexBufferLayout(
			VertexBufferLayoutParts.COLOUR,
			VertexBufferLayoutParts.POSITION,
			VertexBufferLayoutParts.TEXCOORD,
			VertexBufferLayoutParts.TEXUNIT
		)

		final Colour colour
		final Vector2f position
		final Vector2f texCoord
		final float texUnit
	}

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
		batchVertexArrayId = checkForError { -> glGenVertexArrays() }
		checkForError { -> glBindVertexArray(batchVertexArrayId) }

		batchVertexBufferId = checkForError { -> glGenBuffers() }
		checkForError { -> glBindBuffer(GL_ARRAY_BUFFER, batchVertexBufferId) }
		checkForError { -> glBufferData(GL_ARRAY_BUFFER, MAX_VERTICES * Vertex.LAYOUT.sizeInBytes(), GL_DYNAMIC_DRAW) }

		checkForError { ->
			setVertexBufferLayout(renderer.textureShader,
				VertexBufferLayoutParts.COLOUR,
				VertexBufferLayoutParts.POSITION,
				VertexBufferLayoutParts.TEXCOORD,
				VertexBufferLayoutParts.TEXUNIT
			)
		}

		// Generate an index buffer that repeats the pattern needed to draw quads up
		// to the size of the buffer
		batchElementBufferId = checkForError { -> glGenBuffers() }
		stackPush().withCloseable { stack ->
			def indexBuffer = stack.mallocInt(MAX_INDICES)
			(MAX_INDICES / 6).times { i ->
				indexBuffer.put(i + 0, i + 1, i + 3, i + 1, i + 2, i + 3)
			}
			indexBuffer.flip()
			checkForError { -> glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, batchElementBufferId) }
			checkForError { -> glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW) }
		}
	}

	@Override
	void close() {

		glDeleteVertexArrays(batchVertexArrayId)
		glDeleteBuffers(batchVertexBufferId, batchElementBufferId)
		renderer.close()
	}

	@Override
	Mesh createSpriteMesh(Rectanglef surface, float repeatX = 1, float repeatY = 1) {

		return checkForError { ->
			trigger(meshCreatedEvent)

			return new Mesh(
				colours: [Colour.WHITE, Colour.WHITE, Colour.WHITE, Colour.WHITE],
				vertices: surface as Vector2f[],
				textureCoordinates: [
					new Vector2f(0, 0),
					new Vector2f(0, repeatY),
					new Vector2f(repeatX, repeatY),
					new Vector2f(repeatX, 0)
				],
				elementType: GL_TRIANGLES,
				elementCount: 6
			)
		}
	}

	@Override
	Texture createTexture(ByteBuffer data, int format, int width, int height, boolean filter = renderer.config.filter) {

		return checkForError { ->
			renderer.createTexture(data, format, width, height, filter)
		}
	}

	@Override
	void drawMaterial(Material material) {

		averageNanos('drawMaterial', 1f, logger) { ->
			checkForError { ->
				def mesh = material.mesh
				def texture = material.texture

				// If there is no space for the next material, flush the current buffers
				if (!(mesh.vertices.size() <= (MAX_VERTICES - batchVertices)) ||
					!(mesh.elementCount <= (MAX_INDICES - batchIndices)) ||
					!(texture && (batchTextureUnit < maxTextureUnits))) {
					flush()
				}

				// TODO: See if the texture is being used in a previous material and so
				//       set the active texture target to that one
				batchMaterials << material
				batchVertices += mesh.vertices.size()
				batchIndices += mesh.elementCount
				if (texture) {
					batchTextureUnit++
				}
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
			checkForError { -> glUseProgram(shader.programId) }

			// Bind each texture to all the available texture slots and build the
			// sampler uniform that will map to the textures
			def samplers = new int[maxTextureUnits]
			batchMaterials.eachWithIndex { material, index ->
				def texture = material.texture
				texture.textureUnit = index
				checkForError { -> glActiveTexture(GL_TEXTURE0 + index) }
				checkForError { -> glBindTexture(GL_TEXTURE_2D, texture.textureId) }
				samplers[index] = index
			}
			def textureLocation = checkForError { -> getUniformLocation(shader, 'u_textures') }
			checkForError { -> glUniform1iv(textureLocation, 0) }

			def modelBuffer = modelMatrix.get(stack.mallocFloat(Matrix4f.FLOATS))
			def modelLocation = checkForError { -> getUniformLocation(shader, 'model') }
			checkForError { -> glUniformMatrix4fv(modelLocation, false, modelBuffer) }

			// Fill the vertex buffer with vertex data from the material
			def vertexBuffer = stack.mallocFloat(Vertex.LAYOUT.size() * batchVertices)
			batchMaterials.each { material ->
				def mesh = material.mesh
				def texture = material.texture

				mesh.vertices.eachWithIndex { vertex, index ->
					def colour = mesh.colours[index]
					def textureCoordinate = mesh.textureCoordinates[index]
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

			checkForError { -> glBindVertexArray(batchVertexArrayId) }
			checkForError { -> glDrawElements(GL_TRIANGLES, batchIndices, GL_UNSIGNED_INT, 0) }
			trigger(drawEvent)

			batchVertices = 0
			batchIndices = 0
			batchTextureUnit = 0
			batchMaterials.clear()
		}
	}
}
