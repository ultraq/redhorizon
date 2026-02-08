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

import nz.net.ultraq.redhorizon.classic.graphics.PalettedSpriteShader.PalettedSpriteShaderContext
import nz.net.ultraq.redhorizon.graphics.GraphicsNode
import nz.net.ultraq.redhorizon.graphics.Shader
import nz.net.ultraq.redhorizon.graphics.Texture
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLTexture

import java.nio.ByteBuffer

/**
 * An alpha layer to apply atop a palette for transparency effects.
 *
 * @author Emanuel Rabina
 */
class AlphaMask extends GraphicsNode<AlphaMask, PalettedSpriteShaderContext> implements AutoCloseable {

	final Class<? extends Shader> shaderClass = PalettedSpriteShader
	final Texture texture

	/**
	 * Constructor, creates the standard alpha mask used in C&C.
	 */
	AlphaMask() {

		var alphaMaskBuffer = ByteBuffer.allocateNative(256 * 4)
		256.times { i ->
			switch (i) {
				case 0 -> alphaMaskBuffer.put(new byte[]{ 0, 0, 0, 0 })
				case 4 -> alphaMaskBuffer.put(new byte[]{ 0, 0, 0, 128 })
				default -> alphaMaskBuffer.put(new byte[]{ 255, 255, 255, 255 })
			}
		}
		alphaMaskBuffer.flip()
		texture = new OpenGLTexture(256, 1, 4, alphaMaskBuffer)
	}

	@Override
	void close() {

		texture.close()
	}

	@Override
	void render(PalettedSpriteShaderContext shaderContext) {

		shaderContext.setAlphaMask(this)
	}
}
