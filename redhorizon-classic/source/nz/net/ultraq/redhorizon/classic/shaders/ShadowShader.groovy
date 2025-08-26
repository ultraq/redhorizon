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

package nz.net.ultraq.redhorizon.classic.shaders

import nz.net.ultraq.redhorizon.engine.graphics.ShaderConfig
import nz.net.ultraq.redhorizon.engine.graphics.SpriteMaterial
import nz.net.ultraq.redhorizon.graphics.Attribute
import nz.net.ultraq.redhorizon.graphics.Shader
import nz.net.ultraq.redhorizon.graphics.Uniform

/**
 * A shader to draw a silhouette of an existing sprite object.
 *
 * @author Emanuel Rabina
 */
class ShadowShader extends ShaderConfig {

	final String name = 'Shadow'
	final String vertexShaderSource = getResourceAsText('nz/net/ultraq/redhorizon/classic/shaders/Shadow.vert.glsl')
	final String fragmentShaderSource = getResourceAsText('nz/net/ultraq/redhorizon/classic/shaders/Shadow.frag.glsl')
	final Attribute[] attributes = [Attribute.POSITION, Attribute.COLOUR, Attribute.TEXTURE_UVS]
	final Uniform[] uniforms = [
		{ Shader shader, SpriteMaterial material, window ->
			shader.setUniform('indexTexture', 0, material.texture)
		},
		{ Shader shader, SpriteMaterial material, window ->
			if (material.spriteMetadataBuffer) {
				material.spriteMetadataBuffer.bind()
			}
			else {
				shader.setUniform('framesHorizontal', material.framesHorizontal)
				shader.setUniform('framesVertical', material.framesVertical)
				shader.setUniform('frameStepX', material.frameStepX)
				shader.setUniform('frameStepY', material.frameStepY)
			}
			shader.setUniform('frame', material.frame)
		}
	]
}
