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
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.ShaderRequest
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.TextureRequest
import nz.net.ultraq.redhorizon.engine.graphics.SpriteSheet
import nz.net.ultraq.redhorizon.engine.graphics.Texture
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

	private Texture paletteAsTexture
	private boolean paletteChanged

	/**
	 * Constructor, build this sprite from a sprite sheet file.
	 */
	PalettedSprite(ImagesFile imagesFile, Palette palette) {

		super(imagesFile)
		this.palette = palette
	}

	/**
	 * Constructor, build this sprite from an existing sprite sheet.
	 */
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
	}

	@Override
	void onSceneAdded(Scene scene) {

		super.onSceneAdded(scene)
		region.setMax(repeatX, repeatY)

		shader = scene
			.requestCreateOrGet(new ShaderRequest(Shaders.palettedSpriteShader))
			.get()
		// TODO: Load the palette once
		paletteAsTexture = scene
			.requestCreateOrGet(new TextureRequest(256, 1, palette.format, palette as ByteBuffer))
			.get()
		material.palette = paletteAsTexture
		material.faction = faction
	}

	@Override
	void onSceneRemoved(Scene scene) {

		scene.requestDelete(material.palette)
	}

	@Override
	void render(GraphicsRenderer renderer) {

		if (material?.palette) {
			material.faction = faction

			if (paletteChanged) {
				renderer.delete(material.palette)
				paletteAsTexture = renderer.createTexture(256, 1, palette.format, palette as ByteBuffer)
				material.palette = paletteAsTexture
				paletteChanged = false
			}

			super.render(renderer)
		}
	}

	void setPalette(Palette palette) {

		this.palette = palette
		paletteChanged = true
	}
}
