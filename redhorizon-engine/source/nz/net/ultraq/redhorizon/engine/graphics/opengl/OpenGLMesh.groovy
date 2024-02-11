/*
 * Copyright 2023, Emanuel Rabina (http://www.ultraq.net.nz/)
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
import nz.net.ultraq.redhorizon.engine.graphics.Mesh
import nz.net.ultraq.redhorizon.engine.graphics.VertexBufferLayout
import nz.net.ultraq.redhorizon.engine.graphics.VertexBufferLayoutPart

import org.joml.Vector2f
import static org.lwjgl.opengl.GL11C.GL_FLOAT
import static org.lwjgl.opengl.GL15C.*
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer
import static org.lwjgl.opengl.GL30C.*
import static org.lwjgl.system.MemoryStack.stackPush

/**
 * An OpenGL implementation of a mesh.
 *
 * @author Emanuel Rabina
 */
class OpenGLMesh extends Mesh {

	final int vertexArrayId
	final int vertexBufferId
	final int elementBufferId

	/**
	 * Constructor, creates a new OpenGL mesh.
	 */
	OpenGLMesh(int type, VertexBufferLayout layout, Colour colour, Vector2f[] vertices, Vector2f[] textureUVs,
		int[] indices) {

		super(type, colour, vertices, textureUVs, indices)

		vertexArrayId = glGenVertexArrays()
		glBindVertexArray(vertexArrayId)

		// Buffer to hold all the vertex data
		vertexBufferId = glGenBuffers()
		glBindBuffer(GL_ARRAY_BUFFER, vertexBufferId)
		stackPush().withCloseable { stack ->
			var vertexBuffer = stack.mallocFloat(layout.size() * vertices.size())
			vertices.eachWithIndex { vertex, index ->
				layout.parts.each { layoutPart ->
					switch (layoutPart) {
						case VertexBufferLayoutPart.POSITION -> vertexBuffer.put(vertex.x, vertex.y)
						case VertexBufferLayoutPart.COLOUR -> vertexBuffer.put(colour.r, colour.g, colour.b, colour.a)
						case VertexBufferLayoutPart.TEXTURE_UVS -> {
							var textureUV = textureUVs[index]
							vertexBuffer.put(textureUV.x, textureUV.y)
						}
						default -> throw new UnsupportedOperationException("Unhandled vertex layout part ${layoutPart.name()}")
					}
				}
			}
			vertexBuffer.flip()
			glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW)

			layout.parts.each { part ->
				glEnableVertexAttribArray(part.location)
				glVertexAttribPointer(part.location, part.size, GL_FLOAT, false, layout.sizeInBytes(), layout.offsetOfInBytes(part))
			}
		}

		// Buffer for all the index data, if applicable
		if (indices) {
			elementBufferId = glGenBuffers()
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, elementBufferId)
			stackPush().withCloseable { stack ->
				var indexBuffer = stack.mallocInt(indices.size())
					.put(indices)
					.flip()
				glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW)
			}
		}
		else {
			elementBufferId = 0
		}
	}

	@Override
	void bind() {

		glBindVertexArray(vertexArrayId)
	}

	@Override
	void close() {

		if (elementBufferId) {
			glDeleteBuffers(elementBufferId)
		}
		glDeleteBuffers(vertexBufferId)
		glDeleteVertexArrays(vertexArrayId)
	}
}
