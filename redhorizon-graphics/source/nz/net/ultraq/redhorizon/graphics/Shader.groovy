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
import org.joml.Vector2fc
import org.joml.Vector4fc

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FromString

/**
 * A shader is a small program that runs on the GPU.
 *
 * @author Emanuel Rabina
 */
interface Shader<TShaderContext extends ShaderContext> extends GraphicsResource {

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
	void setUniform(String name, float[] values)

	/**
	 * Apply {@code int} data to a uniform in this shader.
	 */
	void setUniform(String name, int value)

	/**
	 * Apply {@code int[]} data to a uniform in this shader.
	 */
	void setUniform(String name, int[] values)

	/**
	 * Apply {@code Matrix4fc} data to a uniform in this shader.
	 */
	void setUniform(String name, Matrix4fc value)

	/**
	 * Apply {@code Vector2fc} data to a uniform in this shader.
	 */
	void setUniform(String name, Vector2fc value)

	/**
	 * Apply {@code Vector4fc[]} data to a uniform in this shader.
	 */
	void setUniform(String name, Vector4fc[] values)

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
	void useShader(@ClosureParams(value = FromString, options = 'TShaderContext') Closure closure)
}
