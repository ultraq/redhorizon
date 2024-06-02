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

import nz.net.ultraq.redhorizon.engine.graphics.Shader.ShaderLifecycle

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
	final Attribute[] attributes
	final Uniform[] uniforms
	final ShaderLifecycle lifecycle

	/**
	 * Constructor, create a shader config for building a shader program later.
	 */
	ShaderConfig(String name, String vertexShaderResourcePath, String fragmentShaderResourcePath, List<Attribute> attributes, Uniform... uniforms) {

		this(name, vertexShaderResourcePath, fragmentShaderResourcePath, attributes, uniforms as List<Uniform>, null)
	}

	/**
	 * Constructor, create a shader config with an optional initialization
	 * closure to build any resources the shader needs.
	 */
	ShaderConfig(String name, String vertexShaderResourcePath, String fragmentShaderResourcePath, List<Attribute> attributes,
		List<Uniform> uniforms, ShaderLifecycle lifecycle) {

		this.name = name
		this.vertexShaderSource = getResourceAsStream(vertexShaderResourcePath).withCloseable { it.text }
		this.fragmentShaderSource = getResourceAsStream(fragmentShaderResourcePath).withCloseable { it.text }
		this.attributes = attributes
		this.uniforms = uniforms
		this.lifecycle = lifecycle
	}
}
