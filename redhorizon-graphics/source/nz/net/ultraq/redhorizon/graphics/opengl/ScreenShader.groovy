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

package nz.net.ultraq.redhorizon.graphics.opengl

import nz.net.ultraq.redhorizon.graphics.PostProcessingShaderContext
import nz.net.ultraq.redhorizon.graphics.Texture

/**
 * A shader that just renders a framebuffer to the screen.  Used as the last
 * step of a post-processing render pipeline.
 *
 * @author Emanuel Rabina
 */
class ScreenShader extends OpenGLShader<PostProcessingShaderContext> {

	/**
	 * Constructor, creates the built-in texture-to-screen shader.
	 */
	ScreenShader() {

		super('Screen', 'nz/net/ultraq/redhorizon/graphics/opengl/Screen.glsl')
	}

	@Override
	protected PostProcessingShaderContext createShaderContext() {

		return new PostProcessingShaderContext() {

			@Override
			void setFramebufferTexture(Texture texture) {

				setUniform('framebuffer', 0, texture)
			}
		}
	}
}
