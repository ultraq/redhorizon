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
 * A render context for drawing elements in a scene.
 *
 * @author Emanuel Rabina
 */
interface SceneRenderContext extends RenderContext {

	/**
	 * Set a whole host of material attributes.
	 */
	void setMaterial(Material material)

	/**
	 * Set the model matrix uniform.
	 */
	void setModelMatrix(Matrix4fc model)

	/**
	 * Set the projection matrix uniform.
	 */
	void setProjectionMatrix(Matrix4fc projection)

	/**
	 * Set the view matrix uniform.
	 */
	void setViewMatrix(Matrix4fc view)
}
