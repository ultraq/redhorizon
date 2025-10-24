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

package nz.net.ultraq.redhorizon.classic.graphics

import nz.net.ultraq.redhorizon.graphics.Material
import nz.net.ultraq.redhorizon.graphics.SceneShaderContext
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLShader

import org.joml.Matrix4fc

/**
 * A shader to draw a silhouette of an existing sprite image.
 *
 * @author Emanuel Rabina
 */
class ShadowShader extends OpenGLShader<ShadowShaderContext> {

	/**
	 * Constructor, create the shadow shader.
	 */
	ShadowShader() {

		super('Shadow', 'nz/net/ultraq/redhorizon/classic/graphics/Shadow.glsl')
	}

	@Override
	protected ShadowShaderContext createShaderContext() {

		return new ShadowShaderContext() {

			@Override
			void setMaterial(Material material) {
				setUniform('indexTexture', 0, material.texture)
				setUniform('frameXY', material.frameXY)
			}

			@Override
			void setModelMatrix(Matrix4fc model) {
				setUniform('model', model)
			}

			@Override
			void setProjectionMatrix(Matrix4fc projection) {
				setUniform('projection', projection)
			}

			@Override
			void setViewMatrix(Matrix4fc view) {
				setUniform('view', view)
			}
		}
	}

	/**
	 * A shader context for drawing sprite shadows in a scene.
	 */
	interface ShadowShaderContext extends SceneShaderContext {
	}
}
