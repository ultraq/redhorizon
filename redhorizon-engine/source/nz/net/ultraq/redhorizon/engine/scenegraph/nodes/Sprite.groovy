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
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.SpriteSheetRequest
import nz.net.ultraq.redhorizon.engine.graphics.Material
import nz.net.ultraq.redhorizon.engine.graphics.Mesh
import nz.net.ultraq.redhorizon.engine.graphics.Shader
import nz.net.ultraq.redhorizon.engine.graphics.SpriteSheet
import nz.net.ultraq.redhorizon.engine.graphics.opengl.Shaders
import nz.net.ultraq.redhorizon.engine.scenegraph.GraphicsElement
import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.engine.scenegraph.SceneEvents
import nz.net.ultraq.redhorizon.filetypes.ImageFile
import nz.net.ultraq.redhorizon.filetypes.ImagesFile

import org.joml.Vector2f
import org.joml.primitives.Rectanglef

import groovy.transform.TupleConstructor
import java.nio.ByteBuffer

/**
 * A simple 2D sprite node.  Contains a texture and coordinate data for what
 * parts of that texture to render (ie: the texture represents a larger sprite
 * sheet / texture atlas, so we need to know what part of that to render).
 *
 * @author Emanuel Rabina
 */
class Sprite extends Node<Sprite> implements GraphicsElement {

	final int numImages
	final SpriteSource spriteSource
	final Rectanglef region = new Rectanglef(0, 0, 1, 1)

	protected Mesh mesh
	protected Shader shader
	protected Material material

	/**
	 * Constructor, build a sprite from an image file.
	 */
	Sprite(ImageFile imageFile) {

		this(imageFile.width, imageFile.height, 1, new ImageFileSpriteSource(imageFile))
	}

	/**
	 * Constructor, build a sprite from a sprite sheet file.
	 */
	Sprite(ImagesFile imagesFile) {

		this(imagesFile.width, imagesFile.height, imagesFile.numImages, new SpriteSheetFileSpriteSource(imagesFile))
	}

	/**
	 * Constructor, build a sprite from an existing sprite sheet.
	 */
	Sprite(int width, int height, int numImages, SpriteSheet spriteSheet) {

		this(width, height, numImages, new SpriteSheetSpriteSource(spriteSheet))
	}

	/**
	 * Constructor, create a sprite using any implementation of the
	 * {@link SpriteSource} interface.
	 */
	protected Sprite(int width, int height, int numImages, SpriteSource spriteSource) {

		bounds
			.set(0, 0, width, height)
			.center()
		this.numImages = numImages
		this.spriteSource = spriteSource
	}

	@Override
	void onSceneAdded(Scene scene) {

		spriteSource.onSceneAdded(scene)
		var spriteSheet = spriteSource.spriteSheet
		region.set(spriteSheet[0])

		mesh = scene
			.requestCreateOrGet(new SpriteMeshRequest(bounds, region))
			.get()
		shader = scene
			.requestCreateOrGet(new ShaderRequest(Shaders.spriteShader))
			.get()
		material = new Material(
			texture: spriteSheet.texture
		)
	}

	@Override
	void onSceneRemoved(Scene scene) {

		scene.requestDelete(mesh)
		spriteSource.onSceneRemoved(scene)
	}

	@Override
	void render(GraphicsRenderer renderer) {

		if (mesh && shader && material) {
			mesh.updateTextureUvs(region as Vector2f[])
			renderer.draw(mesh, globalTransform, shader, material)
		}
	}

	/**
	 * Interface for any source from which sprite data can be obtained.
	 */
	static interface SpriteSource extends SceneEvents {

		/**
		 * Called during setup, returns the sprite sheet to use for the sprite.
		 */
		SpriteSheet getSpriteSheet()
	}

	/**
	 * A sprite source using a single image.
	 */
	@TupleConstructor(defaults = false, includes = 'imageFile')
	static class ImageFileSpriteSource implements SpriteSource {

		final ImageFile imageFile
		SpriteSheet spriteSheet

		@Override
		void onSceneAdded(Scene scene) {

			spriteSheet = scene
				.requestCreateOrGet(new SpriteSheetRequest(imageFile.width, imageFile.height, imageFile.format,
					new ByteBuffer[] { imageFile.imageData }))
				.get()
		}

		@Override
		void onSceneRemoved(Scene scene) {

			scene.requestDelete(spriteSheet)
		}
	}

	/**
	 * A sprite source using a sprite sheet.
	 */
	@TupleConstructor(defaults = false, includes = 'imagesFile')
	static class SpriteSheetFileSpriteSource implements SpriteSource {

		final ImagesFile imagesFile
		SpriteSheet spriteSheet

		@Override
		void onSceneAdded(Scene scene) {

			spriteSheet = scene
				.requestCreateOrGet(new SpriteSheetRequest(imagesFile.width, imagesFile.height, imagesFile.format,
					imagesFile.imagesData))
				.get()
		}

		@Override
		void onSceneRemoved(Scene scene) {

			scene.requestDelete(spriteSheet)
		}
	}

	/**
	 * A sprite source using an existing sprite sheet.
	 */
	@TupleConstructor(defaults = false)
	static class SpriteSheetSpriteSource implements SpriteSource {

		final SpriteSheet spriteSheet
	}
}
