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
import nz.net.ultraq.redhorizon.engine.graphics.VertexBufferLayout
import nz.net.ultraq.redhorizon.engine.graphics.VertexBufferLayoutPart
import nz.net.ultraq.redhorizon.events.EventTarget

import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.primitives.Rectanglef
import static org.lwjgl.opengl.GL11C.*

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

	@Delegate(excludes = ['createMesh', 'createSpriteMesh'])
	final OpenGLRenderer renderer

	// This currently works because meshes and materials are created in pairs, so
	// this bundler works on that assumption
	private final List<Mesh> meshes = []
	private final List<Material> materials = []

	@Override
	Tuple2<Mesh, Material> bundle() {

		var allVertices = []
		var allTextureUVs = []
		var allIndices = []
		var indexOffset = 0
		for (var i = 0; i < meshes.size(); i++) {
			var mesh = meshes[i]
			var material = materials[i]
			allVertices.addAll(mesh.vertices.collect { vertex ->
				var position = material.transform.transformPosition(new Vector3f(vertex, 0))
				return new Vector2f(position.x, position.y)
			})
			allTextureUVs.addAll(mesh.textureUVs)
			allIndices.addAll(mesh.indices.collect { index -> index + indexOffset })
			indexOffset += mesh.vertices.size()
		}

		// Return a new mesh and material based off the first ones in the lists
		// which is assumed to be representative of the entire batch

		var templateMesh = meshes.first()
		var mesh = new OpenGLMesh(templateMesh.vertexType, VERTEX_BUFFER_LAYOUT, templateMesh.colour,
			allVertices as Vector2f[], allTextureUVs as Vector2f[], allIndices as int[])

		var templateMaterial = materials.first()
		var material = new Material(
			texture: templateMaterial.texture,
			transform: new Matrix4f()
		)

		return new Tuple2<>(mesh, material)
	}

//	@Override
//	@NamedVariant
//	Material createMaterial(Texture texture = null, Matrix4f transform = null) {
//
//		var material = new Material(texture, transform)
//		materials << material
//		return material
//	}
//
	@Override
	@NamedVariant
	Mesh createMesh(MeshType type, VertexBufferLayout layout, Colour colour, Vector2f[] vertices,
		Vector2f[] textureUVs = null, int[] indices = null) {

		var mesh = new OpenGLMesh(type == MeshType.LINES ? GL_LINES : type == MeshType.LINE_LOOP ? GL_LINE_LOOP : GL_TRIANGLES,
			layout, colour, vertices, textureUVs, indices)
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
