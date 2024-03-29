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

import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.ShaderRequest
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.SpriteMeshRequest
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.TextureRequest
import nz.net.ultraq.redhorizon.engine.graphics.Material
import nz.net.ultraq.redhorizon.engine.graphics.Mesh
import nz.net.ultraq.redhorizon.engine.graphics.Shader
import nz.net.ultraq.redhorizon.engine.graphics.opengl.Shaders
import nz.net.ultraq.redhorizon.engine.scenegraph.GraphicsElement
import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.filetypes.ImageFile

import org.joml.Vector2f
import org.joml.primitives.Rectanglef

/**
 * A simple 2D sprite node.  Contains a texture and coordinate data for what
 * parts of that texture to render (ie: the texture represents a larger sprite
 * sheet / texture atlas, so we need to know what part of that to render).
 *
 * @author Emanuel Rabina
 */
class Sprite extends Node<Sprite> implements GraphicsElement {

	final Rectanglef region = new Rectanglef(0, 0, 1, 1)

	ImageFile imageFile

	protected Mesh mesh
	protected Shader shader
	protected Material material

	/**
	 * Constructor, build a sprite from an image file.
	 */
	Sprite(ImageFile imageFile) {

		this(imageFile.width, imageFile.height)
		this.imageFile = imageFile
	}

	protected Sprite(int width, int height, Rectanglef region = null) {

		bounds
			.set(0, 0, width, height)
			.center()
		if (region) {
			this.region.set(region)
		}
	}

	@Override
	void onSceneAdded(Scene scene) {

		var width = imageFile.width
		var height = imageFile.height
		var format = imageFile.format

		mesh = scene
			.requestCreateOrGet(new SpriteMeshRequest(bounds))
			.get()
		shader = scene
			.requestCreateOrGet(new ShaderRequest(Shaders.spriteShader))
			.get()
		material = new Material(
			texture: scene
				.requestCreateOrGet(new TextureRequest(width, height, format, imageFile.imageData.flipVertical(width, height, format)))
				.get()
		)
	}

	@Override
	void onSceneRemoved(Scene scene) {

		scene.requestDelete(mesh, material.texture)
	}

	@Override
	void render(GraphicsRenderer renderer) {

		if (mesh && shader && material) {
			mesh.updateTextureUvs(region as Vector2f[])
			renderer.draw(mesh, getGlobalTransform(), shader, material)
		}
	}
}
