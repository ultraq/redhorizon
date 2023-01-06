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

import org.joml.Matrix4f

import groovy.transform.TupleConstructor

/**
 * A shader is a small program that runs on the GPU.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor
abstract class Shader implements AutoCloseable {

	final String name
	final Uniform[] uniforms

	/**
	 * Update a shader's uniforms using the given material.
	 *
	 * @param material
	 */
	void applyMaterial(Material material) {

		uniforms*.apply(this, material)
	}

	/**
	 * Apply a data uniform to the shader.  The type of data is determined by the
	 * size of the data array.
	 *
	 * @param name
	 * @param data
	 */
	abstract void setUniform(String name, float[] data)

	/**
	 * Apply a data uniform to the shader.  The type of data is determined by the
	 * size of the data array.
	 *
	 * @param name
	 * @param data
	 */
	abstract void setUniform(String name, int[] data)

	/**
	 * Apply a matrix uniform to the shader.
	 *
	 * @param name
	 * @param matrix
	 */
	abstract void setUniformMatrix(String name, Matrix4f matrix)

	/**
	 * Apply a texture uniform using the given texture ID.
	 *
	 * @param name
	 * @param textureUnit
	 * @param textureId
	 */
	abstract void setUniformTexture(String name, int textureUnit, Texture texture)

	/**
	 * Return the name of this shader program.
	 *
	 * @return
	 */
	@Override
	String toString() {

		def string = "${name} shader program"
		if (uniforms) {
			string += " (${uniforms.length}, uniforms)"
		}
		return string
	}

	/**
	 * Enable the use of this shader for the next rendering commands.
	 */
	abstract void use()
}
