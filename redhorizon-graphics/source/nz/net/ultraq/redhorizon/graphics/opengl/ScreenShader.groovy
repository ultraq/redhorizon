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

import nz.net.ultraq.redhorizon.graphics.PostProcessingRenderContext
import nz.net.ultraq.redhorizon.graphics.Texture

import static org.lwjgl.opengl.GL11C.*

/**
 * A shader that just renders a framebuffer to the screen.  Used as the last
 * step of a post-processing render pipeline.
 *
 * @author Emanuel Rabina
 */
class ScreenShader extends OpenGLShader<PostProcessingRenderContext> {

	/**
	 * Constructor, creates the built-in texture-to-screen shader.
	 */
	ScreenShader() {

		super('Screen', 'nz/net/ultraq/redhorizon/graphics/opengl/Screen.glsl')
	}

	@Override
	protected PostProcessingRenderContext createRenderContext() {

		return new PostProcessingRenderContext() {

			@Override
			void setFramebufferTexture(Texture texture) {

				setUniform('framebuffer', 0, texture)
			}

			@Override
			void setRenderTarget(RenderTarget renderTarget) {

				renderTarget.use()
				glClear(GL_COLOR_BUFFER_BIT)
				glDisable(GL_DEPTH_TEST)
				var viewport = renderTarget.viewport
				glViewport(viewport.minX, viewport.minY, viewport.lengthX(), viewport.lengthY())
			}
		}
	}
}
