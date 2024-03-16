/*
 * Copyright 2023, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.classic.shaders

import nz.net.ultraq.redhorizon.classic.Faction
import nz.net.ultraq.redhorizon.engine.graphics.Attribute
import nz.net.ultraq.redhorizon.engine.graphics.ShaderConfig

/**
 * A 2D sprite shader for palette-based sprites.
 *
 * @author Emanuel Rabina
 */
class PalettedSpriteShader extends ShaderConfig {

	PalettedSpriteShader() {

		super(
			'PalettedSprite',
			'nz/net/ultraq/redhorizon/classic/shaders/PalettedSprite.vert.glsl',
			'nz/net/ultraq/redhorizon/classic/shaders/PalettedSprite.frag.glsl',
			[Attribute.POSITION, Attribute.COLOUR, Attribute.TEXTURE_UVS],
			{ shader, material, window ->
				shader.setUniformTexture('indexTexture', 0, material.texture)
			},
			{ shader, material, window ->
				shader.setUniformTexture('paletteTexture', 1, material.palette)
			},
			{ shader, material, window ->
				shader.setUniform('factionColours', material.faction?.colours ?: Faction.GOLD.colours)
			}
		)
	}
}
