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

import nz.net.ultraq.redhorizon.classic.Faction
import nz.net.ultraq.redhorizon.classic.shaders.Shaders
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.ShaderRequest
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.SpriteMeshRequest
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.engine.scenegraph.nodes.Sprite
import nz.net.ultraq.redhorizon.filetypes.ImagesFile

import groovy.transform.Memoized
import java.util.concurrent.CompletableFuture

/**
 * A sprite that requires a palette to fully realize its image.  The palette
 * used is a globally-configured one through the {@link GlobalPalette} object.
 *
 * @author Emanuel Rabina
 */
class PalettedSprite extends Sprite implements FactionColours {

	private static final int[] IDENTITY_MAP = 0..255

	final float repeatX
	final float repeatY

	private boolean factionChanged

	/**
	 * Constructor, build this sprite from a sprite sheet file.
	 */
	PalettedSprite(ImagesFile imagesFile) {

		super(imagesFile)
		this.repeatX = 1f
		this.repeatY = 1f
	}

	/**
	 * Constructor, build this sprite from an existing sprite sheet.
	 */
	PalettedSprite(float width, float height, int numImages, SpriteSheetGenerator spriteSheetGenerator) {

		this(width, height, numImages, 1f, 1f, spriteSheetGenerator)
	}

	/**
	 * Constructor, build this sprite from an existing sprite sheet.
	 */
	PalettedSprite(float width, float height, int numImages, float repeatX, float repeatY,
		SpriteSheetGenerator spriteSheetGenerator) {

		super(width, height, numImages, spriteSheetGenerator)

		// TODO: Should be able to move repeat values to Sprite class
		this.repeatX = repeatX
		this.repeatY = repeatY
	}

	/**
	 * Builds an array that is used as an adjustment map to redirect palette
	 * lookups to their intended colours.  eg: palette indices 80-95 are used for
	 * faction colours, so through the fragment shader we want to redirect faction
	 * colour lookups so they land in a different area of the palette.
	 */
	@Memoized
	protected static int[] buildAdjustmentMap(Faction faction) {

		var adjustmentMap = Arrays.copyOf(IDENTITY_MAP, 256)
		(80..95).eachWithIndex { i, j -> adjustmentMap[i] = faction.colours[j] }
		return adjustmentMap
	}

	@Override
	CompletableFuture<Void> onSceneAdded(Scene scene) {

		return CompletableFuture.allOf(
			scene
				.requestCreateOrGet(new SpriteMeshRequest(bounds, region))
				.thenAcceptAsync { newMesh ->
					mesh = newMesh
				},
			scene
				.requestCreateOrGet(new ShaderRequest(Shaders.palettedSpriteShader))
				.thenAcceptAsync { requestedShader ->
					shader = requestedShader
				},
			spriteSheetGenerator.generate(scene)
				.thenApplyAsync { newSpriteSheet ->
					spriteSheet = newSpriteSheet
					material.texture = spriteSheet.texture

					// TODO: Some uses are a repeating tile, others aren't.  There should be a unified way of doing this 🤔
					if (repeatX != 1f || repeatY != 1f) {
						region.setMax(repeatX, repeatY)
					}
					else {
						region.set(spriteSheet[initialFrame])
					}
				},
			CompletableFuture.runAsync { ->
				material.adjustmentMap = buildAdjustmentMap(faction)
			}
		)
	}

	@Override
	void render(GraphicsRenderer renderer) {

		if (material.adjustmentMap) {
			if (factionChanged) {
				material.adjustmentMap = buildAdjustmentMap(faction)
				factionChanged = false
			}
			super.render(renderer)
		}
	}

	/**
	 * Update the faction being applied to this sprite.
	 */
	@Override
	void setFaction(Faction faction) {

		FactionColours.super.setFaction(faction)
		factionChanged = true
	}
}
