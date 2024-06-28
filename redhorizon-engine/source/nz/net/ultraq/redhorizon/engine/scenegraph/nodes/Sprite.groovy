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
import nz.net.ultraq.redhorizon.filetypes.ImageFile
import nz.net.ultraq.redhorizon.filetypes.ImagesFile

import java.util.concurrent.CompletableFuture

/**
 * A simple 2D sprite node.  Contains a texture and coordinate data for what
 * parts of that texture to render (ie: the texture represents a larger sprite
 * sheet / texture atlas, so we need to know what part of that to render).
 *
 * @author Emanuel Rabina
 */
class Sprite extends Node<Sprite> implements GraphicsElement {

	final int numImages
	final SpriteSheetGenerator spriteSheetGenerator

	/**
	 * The frame to select from the underlying spritesheet.
	 */
	int frame = 0

	SpriteSheet spriteSheet

	protected Mesh mesh
	protected Shader shader
	protected Material material = new Material()

	/**
	 * Constructor, build a sprite from an image file.
	 */
	Sprite(ImageFile imageFile) {

		this(imageFile.width, imageFile.height, 1, { scene ->
			return scene.requestCreateOrGet(new SpriteSheetRequest(imageFile.width, imageFile.height, imageFile.format,
				imageFile.imageData))
		})
	}

	/**
	 * Constructor, build a sprite from a sprite sheet file.
	 */
	Sprite(ImagesFile imagesFile) {

		this(imagesFile.width, imagesFile.height, imagesFile.numImages, { scene ->
			return scene.requestCreateOrGet(new SpriteSheetRequest(imagesFile.width, imagesFile.height, imagesFile.format,
				imagesFile.imagesData))
		})
	}

	/**
	 * Constructor, create a sprite using any implementation of the
	 * {@link SpriteSheetGenerator} interface.
	 */
	protected Sprite(float width, float height, int numImages, SpriteSheetGenerator spriteSheetGenerator) {

		bounds.set(0, 0, width, height)
		this.numImages = numImages
		this.spriteSheetGenerator = spriteSheetGenerator
	}

	@Override
	CompletableFuture<Void> onSceneAddedAsync(Scene scene) {

		return CompletableFuture.allOf(
			spriteSheetGenerator.generate(scene)
				.thenComposeAsync { newSpriteSheet ->
					spriteSheet = newSpriteSheet
					material.with {
						texture = spriteSheet.texture
						framesHorizontal = spriteSheet.framesHorizontal
						framesVertical = spriteSheet.framesVertical
						frameStepX = spriteSheet.frameStepX
						frameStepY = spriteSheet.frameStepY
						frame = this.frame
					}
					return scene.requestCreateOrGet(new SpriteMeshRequest(bounds, spriteSheet.textureRegion))
				}
				.thenAcceptAsync { newMesh ->
					mesh = newMesh
				},
			scene
				.requestCreateOrGet(new ShaderRequest(Shaders.spriteShader))
				.thenAcceptAsync { requestedShader ->
					shader = requestedShader
				}
		)
	}

	@Override
	CompletableFuture<Void> onSceneRemovedAsync(Scene scene) {

		return scene.requestDelete(mesh, spriteSheet)
	}

	@Override
	void render(GraphicsRenderer renderer) {

		if (mesh && shader && material.texture) {
			renderer.draw(mesh, globalTransform, shader, material)
		}
	}

	@Override
	void update() {

		material.frame = frame
	}

	/**
	 * Interface for any source from which sprite data can be obtained.
	 */
	@FunctionalInterface
	protected static interface SpriteSheetGenerator {

		/**
		 * Called during setup, returns the sprite sheet to use for the sprite.
		 */
		CompletableFuture<SpriteSheet> generate(Scene scene)
	}
}
