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

import nz.net.ultraq.redhorizon.engine.graphics.GraphicsElement
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.ShaderRequest
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.SpriteMeshRequest
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.TextureRequest
import nz.net.ultraq.redhorizon.engine.graphics.Material
import nz.net.ultraq.redhorizon.engine.graphics.Mesh
import nz.net.ultraq.redhorizon.engine.graphics.Shader
import nz.net.ultraq.redhorizon.engine.graphics.opengl.SpriteShader
import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.filetypes.ImageFile

import org.joml.primitives.Rectanglef

/**
 * A simple 2D sprite node.  Contains a texture and coordinate data for what
 * parts of that texture to render (ie: the texture represents a larger sprite
 * sheet / texture atlas, so we need to know what part of that to render).
 *
 * @author Emanuel Rabina
 */
class Sprite extends Node<Sprite> implements GraphicsElement {

	final ImageFile imageFile

	private Mesh mesh
	private Shader shader
	private Material material
	private Rectanglef region

	Sprite(ImageFile imageFile) {

		bounds.set(0, 0, imageFile.width, imageFile.height)
		this.imageFile = imageFile
	}

	@Override
	void delete(GraphicsRenderer renderer) {
	}

	@Override
	void init(GraphicsRenderer renderer) {
	}

	@Override
	void onSceneAdded(Scene scene) {

		var width = imageFile.width
		var height = imageFile.height
		var format = imageFile.format

		mesh = scene
			.requestCreateOrGet(new SpriteMeshRequest(new Rectanglef(0, 0, width, height)))
			.get()
		shader = scene
			.requestCreateOrGet(new ShaderRequest(SpriteShader.NAME))
			.get()
		material = new Material(
			texture: scene
				.requestCreateOrGet(new TextureRequest(width, height, format, imageFile.imageData.flipVertical(width, height, format)))
				.get()
		)
		region = new Rectanglef(0, 0, width, height)

		super.onSceneAdded(scene)
	}

	@Override
	void onSceneRemoved(Scene scene) {

		scene.requestDelete(mesh, shader)
	}

	@Override
	void render(GraphicsRenderer renderer) {

		if (!mesh || !shader || !material) {
			return
		}

		renderer.draw(mesh, transform, shader, material)
	}
}
