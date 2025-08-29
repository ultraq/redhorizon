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

package nz.net.ultraq.redhorizon.graphics.opengl

import nz.net.ultraq.redhorizon.graphics.Mesh
import nz.net.ultraq.redhorizon.graphics.Vertex

import static org.lwjgl.opengl.GL11C.*
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
class OpenGLMesh implements Mesh {

	private final Type type
	private final Vertex[] vertices
	final boolean dynamic
	private final int[] index

	private final int mode
	private final int vertexArrayId
	private final int vertexBufferId
	private final int elementBufferId

	/**
	 * Constructor, creates a new OpenGL mesh.
	 */
	OpenGLMesh(Type type, Vertex[] vertices, boolean dynamic, int[] index) {

		this.type = type
		this.vertices = vertices
		this.index = index
		this.dynamic = dynamic

		mode = switch (type) {
			case Type.LINES -> GL_LINES
			case Type.LINE_LOOP -> GL_LINE_LOOP
			case Type.TRIANGLES -> GL_TRIANGLES
			default -> throw new UnsupportedOperationException("Unhandled mesh type ${type.name()}")
		}

		vertexArrayId = glGenVertexArrays()
		glBindVertexArray(vertexArrayId)

		// Buffer to hold all the vertex data
		vertexBufferId = glGenBuffers()
		glBindBuffer(GL_ARRAY_BUFFER, vertexBufferId)
		stackPush().withCloseable { stack ->
			var vertexBuffer = vertices
				.inject(stack.mallocFloat(Vertex.FLOATS * vertices.size())) { vertexBuffer, vertex ->
					return vertexBuffer.put(vertex as float[])
				}
				.flip()
			glBufferData(GL_ARRAY_BUFFER, vertexBuffer, dynamic ? GL_DYNAMIC_DRAW : GL_STATIC_DRAW)
		}

		Vertex.LAYOUT.each { attribute ->
			glEnableVertexAttribArray(attribute.location())
			glVertexAttribPointer(attribute.location(), attribute.size(), GL_FLOAT, false, Vertex.BYTES, attribute.offset())
		}

		if (index) {
			elementBufferId = glGenBuffers()
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, elementBufferId)
			stackPush().withCloseable { stack ->
				glBufferData(GL_ELEMENT_ARRAY_BUFFER, stack.mallocInt(index.size()).put(index).flip(), GL_STATIC_DRAW)
			}
		}
		else {
			elementBufferId = 0
		}
	}

	/**
	 * A mesh is truthy/ready if its array data is valid.
	 */
	boolean asBoolean() {

		return glIsVertexArray(vertexArrayId)
	}

	@Override
	void draw() {

		glBindVertexArray(vertexArrayId)
		if (index) {
			glDrawElements(mode, index.length, GL_UNSIGNED_INT, 0)
		}
		else {
			glDrawArrays(mode, 0, vertices.length)
		}
	}

	@Override
	void close() {

		if (elementBufferId) {
			glDeleteBuffers(elementBufferId)
		}
		glDeleteBuffers(vertexBufferId)
		glDeleteVertexArrays(vertexArrayId)
	}

	@Override
	void updateVertexData(Vertex[] newVertices) {

		if (!dynamic) {
			throw new UnsupportedOperationException('Cannot update textureUVs on a mesh that was created without a dynamic buffer')
		}

		stackPush().withCloseable { stack ->
			glBindBuffer(GL_ARRAY_BUFFER, vertexBufferId)

			newVertices.eachWithIndex { newVertex, index ->
				var vertex = vertices[index]
				if (newVertex != vertex) {
					glBufferSubData(GL_ARRAY_BUFFER, 0, stack.floats(newVertex as float[]))
					vertex.update(newVertex)
				}
			}
		}
	}
}
