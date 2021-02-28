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
import org.joml.Vector3f

/**
 * A material defines how a shape should be rendered, so contains meshes,
 * colours, textures, and shaders.
 * 
 * @author Emanuel Rabina
 */
class Material {

	final Matrix4f model = new Matrix4f()

	Mesh mesh
	// TODO: Create a texture list
	Texture texture
	Texture palette
	Shader shader

	/**
	 * Adjust the scale of this material.
	 * 
	 * @param xyz
	 * @param origin
	 */
	Material scale(float xyz) {

		model.scale(xyz)
		return this
	}

	/**
	 * Adjust the scale of this material.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	Material scale(float x, float y, float z = 1) {

		model.scale(x, y, z)
		return this
	}

	/**
	 * Translates the position of this material.
	 * 
	 * @param offset
	 */
	Material translate(Vector3f offset) {

		model.translate(offset)
		return this
	}

	/**
	 * Translates the position of this material.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	Material translate(float x, float y, float z = 1) {

		model.translate(x, y, z)
		return this
	}
}
