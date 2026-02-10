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
import nz.net.ultraq.redhorizon.graphics.opengl.BasicShader
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLMesh

import org.joml.Vector2f
import org.joml.Vector3f

/**
 * An image, or series of images, and a mesh for drawing those images to the
 * screen.
 *
 * @author Emanuel Rabina
 */
class Sprite extends GraphicsNode<Sprite, SceneShaderContext> implements AutoCloseable {

	private static final int[] index = new int[]{ 0, 1, 2, 2, 3, 0 }
	private static final Vector2f defaultFramePosition = new Vector2f(0, 0)

	final int width
	final int height
	final Class<? extends Shader> shaderClass
	final SpriteSheet spriteSheet
	private final Mesh mesh
	private final Material material

	/**
	 * Constructor, create a new sprite.
	 */
	private Sprite(int width, int height, float frameWidth, float frameHeight, Texture texture,
		Class<? extends Shader> shaderClass) {

		this.width = width
		this.height = height
		this.shaderClass = shaderClass
		mesh = new OpenGLMesh(Type.TRIANGLES, new Vertex[]{
			new Vertex(new Vector3f(-width / 2, -height / 2, 0), Colour.WHITE, new Vector2f(0, 0)),
			new Vertex(new Vector3f(width / 2, -height / 2, 0), Colour.WHITE, new Vector2f(frameWidth, 0)),
			new Vertex(new Vector3f(width / 2, height / 2, 0), Colour.WHITE, new Vector2f(frameWidth, frameHeight)),
			new Vertex(new Vector3f(-width / 2, height / 2, 0), Colour.WHITE, new Vector2f(0, frameHeight))
		}, index)
		material = new Material(texture: texture, frameXY: defaultFramePosition)
	}

	/**
	 * Constructor, create a new sprite out of an existing image.
	 */
	Sprite(Image image, Class<? extends Shader> shaderClass = BasicShader) {

		this(image.width, image.height, 1f, 1f, image.texture, shaderClass)
		spriteSheet = null
	}

	/**
	 * Constructor, create a new sprite from a sprite sheet.
	 */
	Sprite(SpriteSheet spriteSheet, Class<? extends Shader> shaderClass = BasicShader) {

		this(spriteSheet.width, spriteSheet.height,
			spriteSheet.width / spriteSheet.texture.width, spriteSheet.height / spriteSheet.texture.height,
			spriteSheet.texture, shaderClass)
		this.spriteSheet = spriteSheet
	}

	@Override
	void close() {

		mesh.close()
	}

	/**
	 * Draw this sprite, using the currently-bound shader and optionally selecting
	 * a frame in the sprite sheet.
	 */
	void render(SceneShaderContext shaderContext) {

		mesh.render(shaderContext, material, globalTransform)
	}

	/**
	 * Adjust which frame of the sprite sheet is displayed.
	 */
	Sprite withFramePosition(Vector2f framePosition) {

		material.frameXY = framePosition
		return this
	}
}
