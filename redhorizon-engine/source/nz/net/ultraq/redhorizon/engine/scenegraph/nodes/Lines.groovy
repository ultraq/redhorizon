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

import nz.net.ultraq.redhorizon.engine.graphics.Colour
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.MeshRequest
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.ShaderRequest
import nz.net.ultraq.redhorizon.engine.graphics.Mesh
import nz.net.ultraq.redhorizon.engine.graphics.MeshType
import nz.net.ultraq.redhorizon.engine.graphics.Shader
import nz.net.ultraq.redhorizon.engine.graphics.VertexBufferLayout
import nz.net.ultraq.redhorizon.engine.graphics.VertexBufferLayoutPart
import nz.net.ultraq.redhorizon.engine.graphics.opengl.Shaders
import nz.net.ultraq.redhorizon.engine.scenegraph.GraphicsElement
import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene

import org.joml.Vector2f

/**
 * A node for the {@code LINES} primitive of OpenGL.
 *
 * @author Emanuel Rabina
 */
class Lines extends Node<Lines> implements GraphicsElement {

	final Colour colour
	final Vector2f[] lines

	private Mesh mesh
	private Shader shader

	/**
	 * Constructor, create a set of lines for every 2 vectors passed in to this
	 * method.  The first describes the line start, the second describes the line
	 * end.
	 */
	Lines(Colour colour, Vector2f... lines) {

		this.colour = colour

		assert lines.length % 2 == 0 : 'Uneven number of points provided'
		this.lines = lines

		// Set bounds to the min/max X/Y points across all lines
		var minX = Float.MAX_VALUE
		var minY = Float.MAX_VALUE
		var maxX = Float.MIN_VALUE
		var maxY = Float.MIN_VALUE
		lines.each { line ->
			minX = Math.min(minX, line.x())
			minY = Math.min(minY, line.y())
			maxX = Math.max(maxX, line.x())
			maxY = Math.max(maxY, line.y())
		}
		bounds.set(minX, minY, maxX, maxY)
	}

	@Override
	void onSceneAdded(Scene scene) {

		mesh = scene
			.requestCreateOrGet(new MeshRequest(MeshType.LINES,
				new VertexBufferLayout(VertexBufferLayoutPart.COLOUR, VertexBufferLayoutPart.POSITION), colour, lines))
			.get()
		shader = scene
			.requestCreateOrGet(new ShaderRequest(Shaders.primitivesShader))
			.get()
	}

	@Override
	void onSceneRemoved(Scene scene) {

		scene.requestDelete(mesh)
	}

	@Override
	void render(GraphicsRenderer renderer) {

		if (!mesh || !shader) {
			return
		}

		renderer.draw(mesh, globalTransform, shader)
	}
}
