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
import nz.net.ultraq.redhorizon.classic.graphics.PalettedSpriteMaterial
import nz.net.ultraq.redhorizon.classic.shaders.Shaders
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.ShaderRequest
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.UniformBufferRequest
import nz.net.ultraq.redhorizon.engine.graphics.SpriteMaterial
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.engine.scenegraph.nodes.Sprite
import nz.net.ultraq.redhorizon.filetypes.ImagesFile
import nz.net.ultraq.redhorizon.graphics.Shader

import groovy.transform.Memoized
import java.nio.IntBuffer
import java.util.concurrent.CompletableFuture

/**
 * A sprite that requires a palette to fully realize its image.  The palette
 * used is a globally-configured one through the {@link GlobalPalette} object.
 *
 * @author Emanuel Rabina
 */
class PalettedSprite extends Sprite implements FactionColours {

	private static final int[] IDENTITY_MAP = 0..255

	private boolean factionChanged
	private boolean adjustmentMapChanged
	private final PalettedSpriteMaterial palettedSpriteMaterial = new PalettedSpriteMaterial()

	/**
	 * Constructor, build this sprite from a sprite sheet file.
	 */
	PalettedSprite(ImagesFile imagesFile) {

		super(imagesFile)
	}

	/**
	 * Constructor, build this sprite from an existing sprite sheet.
	 */
	PalettedSprite(float width, float height, int numImages, float repeatX, float repeatY,
		SpriteSheetGenerator spriteSheetGenerator) {

		super(width, height, numImages, repeatX, repeatY, spriteSheetGenerator)
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
	SpriteMaterial getMaterial() {

		return palettedSpriteMaterial
	}

	@Override
	protected CompletableFuture<Shader> generateShader(Scene scene) {

		return scene.requestCreateOrGet(new ShaderRequest(Shaders.palettedSpriteShader))
	}

	@Override
	CompletableFuture<Void> onSceneAddedAsync(Scene scene) {

		return CompletableFuture.allOf(
			super.onSceneAddedAsync(scene),
			CompletableFuture.completedFuture(buildAdjustmentMap(faction))
				.thenComposeAsync { adjustmentMap ->
					material.adjustmentMap = adjustmentMap

					// This is an int[] in the shader, which on macOS will use a layout
					// like that of std140 with a tonne of padding 😭  As such, we need to
					// include the padding here.  See rule 4 of the standard layout in
					// http://www.opengl.org/registry/doc/glspec45.core.pdf#page=159
					var paletteMetadataBuffer = IntBuffer.allocate(256 * 4) // Each array item will take up 16 bytes, 4x the size of an int
					adjustmentMap.each { adjustment ->
						paletteMetadataBuffer.put(adjustment).advance(3)
					}
					paletteMetadataBuffer.flip()

					return scene.requestCreateOrGet(new UniformBufferRequest('PaletteMetadata', paletteMetadataBuffer))
				}
				.thenAcceptAsync { newUniformBuffer ->
					material.paletteMetadataBuffer = newUniformBuffer
				}
		)
	}

	@Override
	CompletableFuture<Void> onSceneRemovedAsync(Scene scene) {

		return CompletableFuture.allOf(
			super.onSceneRemovedAsync(scene),
			scene.requestDelete(material.paletteMetadataBuffer)
		)
	}

	@Override
	void render(GraphicsRenderer renderer) {

		if (mesh && shader && material.texture) {
			if (adjustmentMapChanged) {
				var paletteMetadataBuffer = IntBuffer.allocate(256 * 4)
				material.adjustmentMap.each { adjustment ->
					paletteMetadataBuffer.put(adjustment).advance(3)
				}
				paletteMetadataBuffer.flip()
				material.paletteMetadataBuffer.updateBufferData(paletteMetadataBuffer, 0)
				adjustmentMapChanged = false
			}
			renderer.draw(mesh, globalTransform, shader, material)
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

	@Override
	void update(float delta) {

		if (factionChanged) {
			material.adjustmentMap = buildAdjustmentMap(faction)
			adjustmentMapChanged = true
			factionChanged = false
		}
		super.update(delta)
	}
}
