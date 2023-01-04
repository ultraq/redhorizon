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

import nz.net.ultraq.redhorizon.engine.graphics.Colour
import nz.net.ultraq.redhorizon.engine.graphics.Material
import nz.net.ultraq.redhorizon.engine.graphics.MaterialBundler
import nz.net.ultraq.redhorizon.engine.graphics.Mesh
import nz.net.ultraq.redhorizon.engine.graphics.MeshType
import nz.net.ultraq.redhorizon.engine.graphics.Texture
import nz.net.ultraq.redhorizon.engine.graphics.VertexBufferLayout
import nz.net.ultraq.redhorizon.engine.graphics.VertexBufferLayoutPart
import nz.net.ultraq.redhorizon.events.EventTarget
import static nz.net.ultraq.redhorizon.engine.graphics.opengl.OpenGLRenderer.enableVertexBufferLayout

import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.primitives.Rectanglef
import static org.lwjgl.opengl.GL15C.*
import static org.lwjgl.opengl.GL30C.glBindVertexArray
import static org.lwjgl.opengl.GL30C.glGenVertexArrays
import static org.lwjgl.system.MemoryStack.stackPush

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

	@Delegate(excludes = ['createMaterial', 'createMesh', 'createSpriteMesh'])
	final OpenGLRenderer renderer

	// This currently works because meshes and materials are created in pairs, so
	// this bundler works on that assumption
	private final List<Mesh> meshes = []
	private final List<Material> materials = []

	@Override
	Tuple2<Mesh, Material> bundle() {

		return stackPush().withCloseable { stack ->
			def vertexArrayId = glGenVertexArrays()
			glBindVertexArray(vertexArrayId)

			// Create and pass to OpenGL buffers that contain all of the materials
			// created with this builder
			def allVertices = []
			def allIndices = []
			meshes.each { mesh ->
				allVertices.addAll(mesh.vertices)
				allIndices.addAll(mesh.indices)
			}
			def vertexBuffer = stack.mallocFloat(allVertices.size() * VERTEX_BUFFER_LAYOUT.size())
			def indexBuffer = stack.mallocInt(allIndices.size())
			def indexOffset = 0

			for (var i = 0; i < meshes.size(); i++) {
				var mesh = meshes[i]
				var material = materials[i]
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

			// Return a new mesh and material based off the first ones in the lists
			// which is assumed to be representative of the entire batch
			var templateMesh = meshes.first()
			var templateMaterial = materials.first()
			var mesh = new Mesh(
				vertexType: templateMesh.vertexType,
				colour: templateMesh.colour,
				vertices: allVertices,
				vertexArrayId: vertexArrayId,
				vertexBufferId: vertexBufferId,
				indices: allIndices,
				elementBufferId: elementBufferId
			)
			var material = new Material(
				texture: templateMaterial.texture,
				transform: new Matrix4f()
			)

			return new Tuple2<>(mesh, material)
		}
	}

	@Override
	@NamedVariant
	Material createMaterial(Texture texture = null, Matrix4f transform = null) {

		def material = renderer.createMaterial(texture, transform)
		materials << material
		return material
	}

	@Override
	@NamedVariant
	Mesh createMesh(MeshType type, VertexBufferLayout layout, Colour colour, Vector2f[] vertices,
		Vector2f[] textureUVs = null, int[] indices = null) {

		var mesh = renderer.createMesh(type, layout, colour, vertices, textureUVs, indices)
		meshes << mesh
		return mesh
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
}
