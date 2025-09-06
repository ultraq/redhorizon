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

package nz.net.ultraq.redhorizon.graphics

import org.joml.Matrix4fc

/**
 * A shader is a small program that runs on the GPU.
 *
 * @author Emanuel Rabina
 */
interface Shader extends ShaderContext, GraphicsResource {

	/**
	 * The name of this shader.
	 */
	String getName()

	/**
	 * Apply {@code float} data to a uniform in this shader.
	 */
	void setUniform(String name, float value)

	/**
	 * Apply {@code float[]} data to a uniform in this shader.
	 */
	void setUniform(String name, float[] value)

	/**
	 * Apply {@code int} data to a uniform in this shader.
	 */
	void setUniform(String name, int value)

	/**
	 * Apply {@code int[]} data to a uniform in this shader.
	 */
	void setUniform(String name, int[] value)

	/**
	 * Apply {@code Matrix4fc} data to a uniform in this shader.
	 */
	void setUniform(String name, Matrix4fc value)

	/**
	 * Apply a texture uniform using the given texture ID.
	 */
	void setUniform(String name, int textureUnit, Texture texture)

	/**
	 * Return the name of this shader program.
	 */
	@Override
	default String toString() {

		return "${name} shader program"
	}

	/**
	 * Enable the use of this shader for the next rendering commands.
	 */
	void use()
}
