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

import nz.net.ultraq.redhorizon.engine.graphics.Material
import nz.net.ultraq.redhorizon.engine.graphics.Shader
import nz.net.ultraq.redhorizon.engine.graphics.ShaderConfig

/**
 * Config for shaders used by this module.
 *
 * @author Emanuel Rabina
 */
class ShaderConfigs {

	static final ShaderConfig PALETTED_SHADER = new ShaderConfig(
		'PalettedSprite',
		'nz/net/ultraq/redhorizon/classic/shaders/PalettedSprite.vert.glsl',
		'nz/net/ultraq/redhorizon/classic/shaders/PalettedSprite.frag.glsl',
		{ Shader shader, Material material ->
			shader.setUniformTexture('indexTexture', 0, material.texture)
		},
		{ Shader shader, Material material ->
			shader.setUniformTexture('paletteTexture', 1, material.palette)
		},
		{ Shader shader, Material material ->
			shader.setUniform('factionColours', material.faction.colours)
		},
		{ Shader shader, Material material ->
			shader.setUniformMatrix('model', material.transform)
		}
	)
}
