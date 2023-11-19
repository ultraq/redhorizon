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
import nz.net.ultraq.redhorizon.engine.graphics.Uniform

/**
 * Configuration for the Scanlines shader.
 *
 * @author Emanuel Rabina
 */
class ScanlinesShader extends ShaderConfig {

	public static final String NAME = 'Scanlines'

	/**
	 * Constructor, create the scanline shader with the given uniforms.
	 */
	ScanlinesShader(Uniform... uniforms) {

		super(
			NAME,
			'nz/net/ultraq/redhorizon/engine/graphics/pipeline/Scanlines.vert.glsl',
			'nz/net/ultraq/redhorizon/engine/graphics/pipeline/Scanlines.frag.glsl',
			uniforms
		)
	}
}
