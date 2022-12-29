/* 
 * Copyright 2021, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import groovy.transform.MapConstructor

/**
 * A shader is a small program that runs on the GPU.
 * 
 * @author Emanuel Rabina
 */
@MapConstructor
@SuppressWarnings('GrFinalVariableAccess')
abstract class Shader {

	final int programId

	final String name
	final Uniform[] uniforms

	/**
	 * Return the name of this shader program.
	 * 
	 * @return
	 */
	@Override
	String toString() {

		def string = "${name} shader program"
		if (uniforms) {
			string += ", uniforms: ${uniforms.collect { it.name }}"
		}
		return string
	}

	/**
	 * Return a configuration object that can be used to adjust this shader in
	 * preparation for rendering.
	 * 
	 * @return
	 */
	abstract ShaderUniformConfig withShaderUniformConfig()
}
