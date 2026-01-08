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

package nz.net.ultraq.redhorizon.engine.graphics

import nz.net.ultraq.redhorizon.engine.Component
import nz.net.ultraq.redhorizon.graphics.SceneShaderContext
import nz.net.ultraq.redhorizon.graphics.Shader

/**
 * A component for adding graphics to an entity.
 *
 * @author Emanuel Rabina
 */
interface GraphicsComponent<T extends GraphicsComponent, SC extends SceneShaderContext>
	extends Component<T> {

	/**
	 * Return the shader used for rendering this component.
	 */
	Class<? extends Shader> getShaderClass()

	/**
	 * Render this component for the current shader context.
	 */
	void render(SC shaderContext)
}
