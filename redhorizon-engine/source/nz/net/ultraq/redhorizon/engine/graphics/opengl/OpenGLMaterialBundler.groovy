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

import nz.net.ultraq.redhorizon.engine.graphics.Material
import nz.net.ultraq.redhorizon.engine.graphics.MaterialBundler
import nz.net.ultraq.redhorizon.engine.graphics.Mesh
import nz.net.ultraq.redhorizon.engine.graphics.Shader
import nz.net.ultraq.redhorizon.engine.graphics.Texture
import nz.net.ultraq.redhorizon.engine.graphics.VertexBufferLayout
import nz.net.ultraq.redhorizon.engine.graphics.VertexBufferLayoutPart
import nz.net.ultraq.redhorizon.events.EventTarget
import static nz.net.ultraq.redhorizon.engine.graphics.opengl.OpenGLRenderer.enableVertexBufferLayout

import org.joml.Matrix4f
import org.joml.Vector3f
import static org.lwjgl.opengl.GL15C.*
import static org.lwjgl.opengl.GL30C.glBindVertexArray
import static org.lwjgl.opengl.GL30C.glGenVertexArrays
import static org.lwjgl.system.MemoryStack.stackPush

import groovy.transform.NamedParam
import groovy.transform.NamedVariant
import groovy.transform.TupleConstructor

/**
 * A material builder for OpenGL.
 * 
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class OpenGLMaterialBundler implements MaterialBundler, EventTarget {

	// TODO: Derive the layout from the materials being bundled, not hard-coded in this class
	private static final VertexBufferLayout VERTEX_BUFFER_LAYOUT = new VertexBufferLayout(
		VertexBufferLayoutPart.COLOUR,
		VertexBufferLayoutPart.POSITION,
		VertexBufferLayoutPart.TEXTURE_UVS
	)

	@Delegate
	final OpenGLRenderer renderer

	private final List<Material> materials = []

	@Override
	Material bundle() {

		return stackPush().withCloseable { stack ->
			def vertexArrayId = glGenVertexArrays()
			glBindVertexArray(vertexArrayId)

			// Create and pass to OpenGL buffers that contain all of the materials
			// created with this builder
			def allVertices = []
			def allIndices = []
			materials.each { material ->
				allVertices.addAll(material.mesh.vertices)
				allIndices.addAll(material.mesh.indices)
			}
			def vertexBuffer = stack.mallocFloat(allVertices.size() * VERTEX_BUFFER_LAYOUT.size())
			def indexBuffer = stack.mallocInt(allIndices.size())
			def indexOffset = 0

			materials.each { material ->
				def mesh = material.mesh
				def colour = mesh.colour
				def textureUVs = mesh.textureUVs
				mesh.vertices.eachWithIndex { vertex, vertexIndex ->
					def position = material.transform.transformPosition(new Vector3f(vertex, 0))
					def textureUV = textureUVs[vertexIndex]
					vertexBuffer.put(
						colour.r, colour.g, colour.b, colour.a,
						position.x, position.y,
						textureUV.x, textureUV.y
					)
				}
				mesh.indices.each { index ->
					indexBuffer.put(index + indexOffset)
				}
				indexOffset += mesh.vertices.size()
			}
			vertexBuffer.flip()
			indexBuffer.flip()

			def vertexBufferId = glGenBuffers()
			glBindBuffer(GL_ARRAY_BUFFER, vertexBufferId)
			glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW)

			enableVertexBufferLayout(VERTEX_BUFFER_LAYOUT)

			def elementBufferId = glGenBuffers()
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, elementBufferId)
			glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW)

			// Return a new material based off the first one in the list which is
			// assumed to be representative of the entire batch
			def templateMaterial = materials.first()
			return new Material(
				mesh: new Mesh(
					vertexType: templateMaterial.mesh.vertexType,
					colour: templateMaterial.mesh.colour,
					vertices: allVertices,
					vertexArrayId: vertexArrayId,
					vertexBufferId: vertexBufferId,
					indices: allIndices,
					elementBufferId: elementBufferId
				),
				texture: templateMaterial.texture,
				shader: templateMaterial.shader,
				transform: new Matrix4f()
			)
		}
	}

	@NamedVariant
	@Override
	Material createMaterial(@NamedParam(required = true) Mesh mesh, @NamedParam Texture texture,
		@NamedParam Shader shader, @NamedParam Matrix4f transform) {

		def material = renderer.createMaterial(
			mesh: mesh,
			texture: texture,
			shader: shader,
			transform: transform
		)
		materials << material
		return material
	}
}
