/*
 * Copyright 2026, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine.debug

import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.GraphicsNode
import nz.net.ultraq.redhorizon.graphics.Mesh
import nz.net.ultraq.redhorizon.graphics.Mesh.Type
import nz.net.ultraq.redhorizon.graphics.SceneShaderContext
import nz.net.ultraq.redhorizon.graphics.Shader
import nz.net.ultraq.redhorizon.graphics.Vertex
import nz.net.ultraq.redhorizon.graphics.opengl.BasicShader
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLMesh

import org.joml.Vector2fc
import org.joml.Vector3f

/**
 * A line to visualize a vector.
 *
 * @author Emanuel Rabina
 */
class MovementLine extends GraphicsNode<MovementLine, SceneShaderContext> {

	final Class<? extends Shader> shaderClass = BasicShader
	private final Vector2fc vector
	private final float factor
	private final Colour colour
	private final Mesh mesh

	/**
	 * Constructor, set the vector that this line should track.
	 */
	MovementLine(Vector2fc vector, float factor, Colour colour) {

		this.vector = vector
		this.factor = factor
		this.colour = colour
		this.mesh = new OpenGLMesh(Type.LINES, [
			new Vertex(new Vector3f(), colour),
			new Vertex(new Vector3f(vector.x(), vector.y(), 0f).mul(factor), colour)
		] as Vertex[], null, true)
	}

	@Override
	void render(SceneShaderContext shaderContext) {

		// TODO: Selectively update parts of the vertex
		mesh.updateVertexData([
			new Vertex(new Vector3f(), colour),
			new Vertex(new Vector3f(vector.x(), vector.y(), 0f).mul(factor), colour)
		] as Vertex[])
		mesh.render(shaderContext, null, globalTransform)
	}
}
