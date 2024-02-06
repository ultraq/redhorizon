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

package nz.net.ultraq.redhorizon.engine.graphics

/**
 * The configuration for a shader program, can be used to build shaders with the
 * renderer.
 *
 * @author Emanuel Rabina
 */
class ShaderConfig {

	final String name
	final String vertexShaderSource
	final String fragmentShaderSource
	final Uniform[] uniforms

	/**
	 * Constructor, create a shader config for building a shader program later.
	 *
	 * @param name
	 * @param vertexShaderResourcePath
	 * @param fragmentShaderResourcePath
	 * @param uniforms
	 */
	ShaderConfig(String name, String vertexShaderResourcePath, String fragmentShaderResourcePath, Uniform... uniforms) {

		this.name = name
		this.vertexShaderSource = getResourceAsStream(vertexShaderResourcePath).text
		this.fragmentShaderSource = getResourceAsStream(fragmentShaderResourcePath).text
		this.uniforms = uniforms
	}
}
