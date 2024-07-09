/*
 * Copyright 2024, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine.scenegraph.nodes

import nz.net.ultraq.redhorizon.engine.graphics.Attribute
import nz.net.ultraq.redhorizon.engine.graphics.Colour
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.MeshRequest
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.ShaderRequest
import nz.net.ultraq.redhorizon.engine.graphics.Mesh
import nz.net.ultraq.redhorizon.engine.graphics.MeshType
import nz.net.ultraq.redhorizon.engine.graphics.Shader
import nz.net.ultraq.redhorizon.engine.graphics.VertexBufferLayout
import nz.net.ultraq.redhorizon.engine.graphics.opengl.Shaders
import nz.net.ultraq.redhorizon.engine.scenegraph.GraphicsElement
import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene

import org.joml.Matrix4f
import org.joml.Vector2f

import java.util.concurrent.CompletableFuture

/**
 * A node for creating a mesh using any of the OpenGL primitives.
 *
 * @author Emanuel Rabina
 */
class Primitive extends Node<Primitive> implements GraphicsElement {

	final MeshType type
	final Colour colour
	Vector2f[] points
	final boolean dynamic

	protected Mesh mesh
	protected Shader shader
	protected final Matrix4f transformCopy = new Matrix4f()
	protected boolean pointsChanged

	/**
	 * Constructor, create a set of lines for every 2 vectors passed in to this
	 * method.  The first describes the line start, the second describes the line
	 * end.
	 */
	Primitive(MeshType type, Colour colour, Vector2f[] points, boolean dynamic = false) {

		this.type = type
		this.colour = colour
		this.points = points
		this.dynamic = dynamic

		recalculateBounds()
	}

	@Override
	CompletableFuture<Void> onSceneAddedAsync(Scene scene) {

		return CompletableFuture.allOf(
			scene
				.requestCreateOrGet(type == MeshType.TRIANGLES ?
					new MeshRequest(type, new VertexBufferLayout(Attribute.POSITION, Attribute.COLOUR), this.points, colour,
						[0, 1, 3, 1, 2, 3] as int[], dynamic) :
					new MeshRequest(type, new VertexBufferLayout(Attribute.POSITION, Attribute.COLOUR), this.points, colour, dynamic))
				.thenAcceptAsync { newMesh ->
					mesh = newMesh
				},
			scene
				.requestCreateOrGet(new ShaderRequest(Shaders.primitivesShader))
				.thenAcceptAsync { requestedShader ->
					shader = requestedShader
				}
		)
	}

	@Override
	CompletableFuture<Void> onSceneRemovedAsync(Scene scene) {

		return scene.requestDelete(mesh)
	}

	/**
	 * Update the bounds of this object against the points used to build it.
	 */
	private void recalculateBounds() {

		var minX = Float.MAX_VALUE
		var minY = Float.MAX_VALUE
		var maxX = Float.MIN_VALUE
		var maxY = Float.MIN_VALUE
		points.each { point ->
			minX = Math.min(minX, point.x())
			minY = Math.min(minY, point.y())
			maxX = Math.max(maxX, point.x())
			maxY = Math.max(maxY, point.y())
		}
		bounds { ->
			set(minX, minY, maxX, maxY)
		}
	}


	@Override
	RenderCommand renderCommand() {

		transformCopy.set(globalTransform)

		return { renderer ->
			if (mesh && pointsChanged) {
				mesh.updateVertices(points)
				pointsChanged = false
			}

			if (mesh && shader) {
				renderer.draw(mesh, transformCopy, shader)
			}
		}
	}

	/**
	 * Update the points of this primitive object.
	 */
	void updatePoints(Vector2f[] newPoints) {

		if (!dynamic) {
			throw new IllegalStateException('Cannot update points of a static mesh')
		}

		points = newPoints
		recalculateBounds()
		pointsChanged = true
	}
}
