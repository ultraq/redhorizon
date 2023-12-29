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

package nz.net.ultraq.redhorizon.engine.graphics.opengl

import nz.net.ultraq.redhorizon.engine.graphics.ShaderConfig

/**
 * A simple 2D sprite shader.
 *
 * @author Emanuel Rabina
 */
class SpriteShader extends ShaderConfig {

	static final String NAME = 'Sprite'
	static final String UNIFORM_MAIN_TEXTURE = 'mainTexture'

	SpriteShader() {

		super(
			NAME,
			'nz/net/ultraq/redhorizon/engine/graphics/opengl/Sprite.vert.glsl',
			'nz/net/ultraq/redhorizon/engine/graphics/opengl/Sprite.frag.glsl',
			{ shader, material, window ->
				shader.setUniformTexture(UNIFORM_MAIN_TEXTURE, 0, material.texture)
			},
			{ shader, material, window ->
				shader.setUniformMatrix(UNIFORM_MODEL, material.transform)
			}
		)
	}
}
