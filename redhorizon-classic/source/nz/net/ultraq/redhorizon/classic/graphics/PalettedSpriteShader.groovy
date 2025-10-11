/*
 * Copyright 2023, Emanuel Rabina (http://www.ultraq.net.nz/)
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
import nz.net.ultraq.redhorizon.graphics.Palette
import nz.net.ultraq.redhorizon.graphics.SceneShaderContext
import nz.net.ultraq.redhorizon.graphics.Texture
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLShader

import org.joml.Matrix4fc

/**
 * A 2D sprite shader for palette-based sprites.
 *
 * @author Emanuel Rabina
 */
class PalettedSpriteShader extends OpenGLShader<PalettedSpriteShaderContext> {

	/**
	 * Constructor, set the shader name and load the shader sources.
	 */
	PalettedSpriteShader() {

		super('PalettedSprite', 'nz/net/ultraq/redhorizon/classic/graphics/PalettedSprite.glsl')
	}

	@Override
	protected PalettedSpriteShaderContext createShaderContext() {

		return new PalettedSpriteShaderContext() {

			@Override
			void setAlphaMask(Texture alphaMask) {
				setUniform('alphaMask', 2, alphaMask)
			}

			@Override
			void setMaterial(Material material) {
				setUniform('indexTexture', 0, material.texture)
				setUniform('frameXY', material.frameXY)
//				setUniform('adjustmentMap', material.adjustmentMap)
			}

			@Override
			void setModelMatrix(Matrix4fc model) {
				setUniform('model', model)
			}

			@Override
			void setPalette(Palette palette) {
				setUniform('palette', 1, palette.texture)
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
	 * A shader context for drawing palette-based sprites in a scene.
	 */
	interface PalettedSpriteShaderContext extends SceneShaderContext {

		/**
		 * Set the alpha mask to apply atop the palette.
		 */
		void setAlphaMask(Texture alphaMask)

		/**
		 * Set the palette to use.
		 */
		void setPalette(Palette palette)
	}
}
