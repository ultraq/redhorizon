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

package nz.net.ultraq.redhorizon.engine.graphics.pipeline

import nz.net.ultraq.redhorizon.engine.graphics.ShaderConfig

/**
 * Configuration for the Sharp Upscaling shader.
 *
 * @author Emanuel Rabina
 */
class SharpUpscalingShader extends ShaderConfig {

	static final String NAME = 'SharpUpscaling'
	static final String UNIFORM_TEXTURE_SOURCE_SIZE = 'textureSourceSize'

	/**
	 * Constructor, create the sharp upscaling shader.
	 */
	SharpUpscalingShader() {

		super(
			NAME,
			'nz/net/ultraq/redhorizon/engine/graphics/pipeline/SharpUpscaling.vert.glsl',
			'nz/net/ultraq/redhorizon/engine/graphics/pipeline/SharpUpscaling.frag.glsl',
			Uniforms.framebufferUniform,
			{ shader, material, window ->
				shader.setUniform(UNIFORM_TEXTURE_SOURCE_SIZE, window.renderResolution as float[])
			},
			Uniforms.textureTargetSizeUniform
		)
	}
}
