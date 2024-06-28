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
@TupleConstructor(defaults = false)
abstract class Shader implements GraphicsResource {

	static final String UNIFORM_MODEL = 'model'

	final String name
	final Attribute[] attributes
	final Uniform[] uniforms

	private final Map<String, Object> lastValues = [:]

	/**
	 * Update a shader's uniforms using the given context.
	 */
	void applyUniforms(Matrix4f transform, Material material, Window window) {

		// Model uniform is universal, so bake this here
		setUniform(UNIFORM_MODEL, transform)

		uniforms*.apply(this, material, window)
	}

	/**
	 * Apply a data uniform to the shader.  If {@code data} is an array, it can be
	 * used to determine the shader type (eg: 2 floats = vec2).
	 */
	void setUniformGeneric(String name, Object data) {

		if (data == null) {
			throw new IllegalArgumentException("Data value for key ${name} was null")
		}

		// Only perform an update if there was a change in the value since last time
		if (lastValues[name] != data) {
			lastValues[name] = data

			switch (data) {
				case Float -> setUniform(name, data)
				case Float[] -> setUniform(name, (float[])data)
				case Integer -> setUniform(name, data)
				case Integer[] -> setUniform(name, (int[])data)
				case Matrix4f -> setUniform(name, data)
				default -> throw new UnsupportedOperationException("Data type of ${data.class.simpleName} not supported")
			}
		}
	}

	/**
	 * Apply a data uniform to the shader.  The type of data is determined by the
	 * size of the data array.
	 */
	abstract void setUniform(String name, float[] data)

	/**
	 * Apply a data uniform to the shader.  The type of data is determined by the
	 * size of the data array.
	 */
	abstract void setUniform(String name, int[] data)

	/**
	 * Apply a matrix uniform to the shader.
	 */
	abstract void setUniform(String name, Matrix4f matrix)

	/**
	 * Apply a texture uniform using the given texture ID.
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
			string += " (${uniforms.length}, uniform(s))"
		}
		return string
	}

	/**
	 * Enable the use of this shader for the next rendering commands.
	 */
	abstract void use()
}
