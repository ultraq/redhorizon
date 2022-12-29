/* 
 * Copyright 2022, Emanuel Rabina (http://www.ultraq.net.nz/)
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
 * A subset of renderer functions for applying uniform values to shaders.
 * 
 * @author Emanuel Rabina
 */
interface ShaderUniformConfig {

	/**
	 * Apply a data uniform to the shader.  The type of data is determined by the
	 * size of the data array.
	 * 
	 * @param location
	 * @param data
	 */
	void setUniform(int location, float[] data)

	/**
	 * Apply a data uniform to the shader.  The type of data is determined by the
	 * size of the data array.
	 *
	 * @param location
	 * @param data
	 */
	void setUniform(int location, int[] data)

	/**
	 * Apply a matrix uniform to the shader.
	 * 
	 * @param location
	 * @param data
	 */
	void setUniformMatrix(int location, float[] data)

	/**
	 * Apply a texture uniform using the given texture ID.
	 * 
	 * @param location
	 * @param textureUnit
	 * @param textureId
	 */
	void setUniformTexture(int location, int textureUnit, int textureId)
}
