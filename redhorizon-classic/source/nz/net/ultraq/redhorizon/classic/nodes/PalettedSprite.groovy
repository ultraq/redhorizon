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

package nz.net.ultraq.redhorizon.classic.nodes

import nz.net.ultraq.redhorizon.classic.shaders.Shaders
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.TextureRequest
import nz.net.ultraq.redhorizon.engine.graphics.SpriteSheet
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.engine.scenegraph.nodes.Sprite
import nz.net.ultraq.redhorizon.filetypes.ImagesFile
import nz.net.ultraq.redhorizon.filetypes.Palette

import java.nio.ByteBuffer

/**
 * A sprite that requires a palette to fully realize its image.
 *
 * @author Emanuel Rabina
 */
class PalettedSprite extends Sprite implements FactionColours {

	final float repeatX
	final float repeatY
	Palette palette

	private boolean paletteChanged

	/**
	 * Constructor, build this sprite from a sprite sheet file.
	 */
	PalettedSprite(ImagesFile imagesFile, Palette palette) {

		super(imagesFile)
		this.repeatX = 1f
		this.repeatY = 1f
		this.palette = palette
		spriteShaderName = Shaders.palettedSpriteShader
	}

	/**
	 * Constructor, build this sprite from an existing sprite sheet.
	 */
	// This constructor is inherited, so check subclasses for its use before deleting
	PalettedSprite(int width, int height, int numImages, SpriteSheet spriteSheet, Palette palette) {

		this(width, height, numImages, spriteSheet, 1f, 1f, palette)
	}

	/**
	 * Constructor, build this sprite from an existing sprite sheet.
	 */
	PalettedSprite(int width, int height, int numImages, SpriteSheet spriteSheet, float repeatX, float repeatY,
		Palette palette) {

		super(width, height, numImages, spriteSheet)
		this.repeatX = repeatX
		this.repeatY = repeatY
		this.palette = palette
		spriteShaderName = Shaders.palettedSpriteShader
	}

	@Override
	void onSceneAdded(Scene scene) {

		super.onSceneAdded(scene)

		// TODO: Some uses are a repeating tile, others aren't.  There should be a unified way of doing this 🤔
		if (repeatX != 1f || repeatY != 1f) {
			region.setMax(repeatX, repeatY)
		}

		// TODO: Load the palette once
		scene
			.requestCreateOrGet(new TextureRequest(256, 1, palette.format, palette as ByteBuffer))
			.thenAcceptAsync { newTexture ->
				material.palette = newTexture
			}
	}

	@Override
	void onSceneRemoved(Scene scene) {

		super.onSceneRemoved(scene)
		scene.requestDelete(material.palette)
	}

	@Override
	void render(GraphicsRenderer renderer) {

		if (material?.palette) {
			material.faction = faction

			if (paletteChanged) {
				renderer.delete(material.palette)
				material.palette = renderer.createTexture(256, 1, palette.format, palette as ByteBuffer)
				paletteChanged = false
			}

			super.render(renderer)
		}
	}

	/**
	 * Update the palette being applied to this sprite.
	 */
	void setPalette(Palette palette) {

		this.palette = palette
		paletteChanged = true
	}
}
