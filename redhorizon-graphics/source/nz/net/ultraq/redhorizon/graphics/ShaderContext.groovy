/*
 * Copyright 2025, Emanuel Rabina (http://www.ultraq.net.nz/)
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
 * The part of the shader that can be given to other graphics objects for the
 * application of uniform data like transforms and textures.
 *
 * @author Emanuel Rabina
 */
interface ShaderContext {

	/**
	 * Update a shader's uniforms using the given context.
	 */
	void applyUniforms(Matrix4fc transform, Material material, Window window)
}
