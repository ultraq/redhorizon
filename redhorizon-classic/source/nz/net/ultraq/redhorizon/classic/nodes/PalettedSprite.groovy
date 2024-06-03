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
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.TextureRequest
import nz.net.ultraq.redhorizon.engine.graphics.Material
import nz.net.ultraq.redhorizon.engine.graphics.Texture
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.engine.scenegraph.nodes.Sprite
import nz.net.ultraq.redhorizon.filetypes.ColourFormat
import nz.net.ultraq.redhorizon.filetypes.ImagesFile
import nz.net.ultraq.redhorizon.filetypes.Palette

import groovy.transform.Memoized
import java.nio.ByteBuffer
import java.util.concurrent.CompletableFuture

/**
 * A sprite that requires a palette to fully realize its image.
 *
 * @author Emanuel Rabina
 */
class PalettedSprite extends Sprite implements FactionColours {

	private static final int[] IDENTITY_MAP = 0..255

	final float repeatX
	final float repeatY

	private Palette palette
	private boolean paletteChanged
	private boolean factionChanged
	private Texture alphaMask

	/**
	 * Constructor, build this sprite from a sprite sheet file.
	 */
	PalettedSprite(ImagesFile imagesFile, Palette palette) {

		super(imagesFile)
		this.repeatX = 1f
		this.repeatY = 1f
		this.palette = palette
	}

	/**
	 * Constructor, build this sprite from an existing sprite sheet.
	 */
	PalettedSprite(float width, float height, int numImages, Palette palette, SpriteSheetGenerator spriteSheetGenerator) {

		this(width, height, numImages, 1f, 1f, palette, spriteSheetGenerator)
	}

	/**
	 * Constructor, build this sprite from an existing sprite sheet.
	 */
	PalettedSprite(float width, float height, int numImages, float repeatX, float repeatY, Palette palette,
		SpriteSheetGenerator spriteSheetGenerator) {

		super(width, height, numImages, spriteSheetGenerator)
		this.repeatX = repeatX
		this.repeatY = repeatY
		this.palette = palette
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

		material = new Material()

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
			// TODO: Load the palette once
			scene
				.requestCreateOrGet(new TextureRequest(256, 1, palette.format, palette as ByteBuffer))
				.thenAcceptAsync { newTexture ->
					material.palette = newTexture
				},
			CompletableFuture.runAsync { ->
				material.adjustmentMap = buildAdjustmentMap(faction)
			},
			// TODO: Load the alpha mask once.  I think for these 'once' objects, I
			//       need a global object and uniform that these shaders use, much
			//       like the camera
			CompletableFuture.supplyAsync { ->
				var alphaMaskData = ByteBuffer.allocateNative(1024)
				(0..255).each { i ->
					alphaMaskData.put(
						switch (i) {
							case 0 -> new byte[]{ 0, 0, 0, 0 }
							case 4 -> new byte[]{ 0, 0, 0, 0x7f }
							default -> new byte[]{ 0xff, 0xff, 0xff, 0xff }
						}
					)
				}
				return alphaMaskData.flip()
			}
				.thenComposeAsync { alphaMaskData ->
					return scene.requestCreateOrGet(new TextureRequest(256, 1, ColourFormat.FORMAT_RGBA, alphaMaskData))
				}
				.thenAcceptAsync { newTexture ->
					material.alphaMask = newTexture
				}
		)
	}

	@Override
	CompletableFuture<Void> onSceneRemoved(Scene scene) {

		return CompletableFuture.allOf(
			super.onSceneRemoved(scene),
			scene.requestDelete(material.palette)
		)
	}

	@Override
	void render(GraphicsRenderer renderer) {

		if (material?.palette && material.adjustmentMap && material?.alphaMask) {
			if (paletteChanged) {
				renderer.delete(material.palette)
				material.palette = renderer.createTexture(256, 1, palette.format, palette as ByteBuffer)
				paletteChanged = false
			}
			if (factionChanged) {
				material.adjustmentMap = buildAdjustmentMap(faction) // TODO: Make this a 1D texture
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

	/**
	 * Update the palette being applied to this sprite.
	 */
	void setPalette(Palette palette) {

		this.palette = palette
		paletteChanged = true
	}
}
