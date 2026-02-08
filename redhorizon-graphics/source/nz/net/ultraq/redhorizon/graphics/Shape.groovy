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

package nz.net.ultraq.redhorizon.graphics

import nz.net.ultraq.redhorizon.graphics.Mesh.Type
import nz.net.ultraq.redhorizon.graphics.opengl.BasicShader
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLMesh

/**
 * A shape is a wireframe mesh.
 *
 * @author Emanuel Rabina
 */
class Shape extends GraphicsNode<Shape, SceneShaderContext> implements AutoCloseable {

	final Mesh mesh
	final Class<? extends Shader> shaderClass = BasicShader

	/**
	 * Constructor, configure this shape.
	 */
	Shape(Type type, Vertex[] vertices, int[] index = null) {

		mesh = new OpenGLMesh(type, vertices, index)
	}

	@Override
	void close() {

		mesh.close()
	}

	@Override
	void render(SceneShaderContext shaderContext) {

		mesh.render(shaderContext, null, globalTransform)
	}
}
