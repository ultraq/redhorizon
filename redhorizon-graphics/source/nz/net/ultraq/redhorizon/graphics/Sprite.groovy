/*
 * Copyright 2025, Emanuel Rabina (http://www.ultraq.net.nz/)
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
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLMesh

import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f

/**
 * An image, or series of images, and a mesh for drawing those images to the
 * screen.
 *
 * @author Emanuel Rabina
 */
class Sprite implements AutoCloseable {

	private final Mesh mesh
	final int width
	final int height
	final Material material
	final Matrix4f transform = new Matrix4f()

	/**
	 * Constructor, create a new sprite out of an existing image.
	 */
	Sprite(Image image, int width = image.width, int height = image.height) {

		this.width = width
		this.height = height
		mesh = new OpenGLMesh(Type.TRIANGLES,
			new Vertex[]{
				new Vertex(new Vector3f(0, 0, 0), Colour.WHITE, new Vector2f(0, 0)),
				new Vertex(new Vector3f(width, 0, 0), Colour.WHITE, new Vector2f(1, 0)),
				new Vertex(new Vector3f(width, height, 0), Colour.WHITE, new Vector2f(1, 1)),
				new Vertex(new Vector3f(0, height, 0), Colour.WHITE, new Vector2f(0, 1))
			},
			new int[]{ 0, 1, 2, 2, 3, 0 }
		)
		material = new Material(texture: image.texture)
	}

	@Override
	void close() {

		mesh?.close()
	}

	/**
	 * Draw this sprite, using the currently-bound shader.
	 */
	void draw(RenderContext renderContext) {

		renderContext.setModelMatrix(transform)
		renderContext.setMaterial(material)
		mesh.draw()
	}
}
