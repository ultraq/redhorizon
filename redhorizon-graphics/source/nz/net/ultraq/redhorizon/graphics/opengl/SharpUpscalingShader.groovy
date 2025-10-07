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
 * A post-processing shader for taking a low-resolution input framebuffer and
 * upscaling it to a target resolution while maintaining the appearance of
 * sharpness.
 *
 * @author Emanuel Rabina
 */
class SharpUpscalingShader extends OpenGLShader<SharpUpscalingShaderContext> {

	/**
	 * Constructor, creates the built-in upscaling shader.
	 */
	SharpUpscalingShader() {

		super('SharpUpscaling', 'nz/net/ultraq/redhorizon/graphics/opengl/SharpUpscaling.glsl')
	}

	@Override
	protected SharpUpscalingShaderContext createShaderContext() {

		return new SharpUpscalingShaderContext()
	}

	/**
	 * A post-processing shader context that requires additional inputs.
	 */
	class SharpUpscalingShaderContext implements PostProcessingShaderContext {

		@Override
		void setFramebufferTexture(Texture texture) {
			setUniform('framebuffer', 0, texture)
		}

		/**
		 * Set the width and height (resolution) of the input framebuffer.
		 */
		void setTextureSourceSize(float[] sourceSize) {
			setUniform('textureSourceSize', sourceSize)
		}

		/**
		 * Set the width and height (resolution) of the output render target.
		 */
		void setTextureTargetSize(float[] targetSize) {
			setUniform('textureTargetSize', targetSize)
		}
	}
}
