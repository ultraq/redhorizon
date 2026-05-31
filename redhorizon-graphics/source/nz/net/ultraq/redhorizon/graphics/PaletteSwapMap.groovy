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

import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLTexture
import nz.net.ultraq.redhorizon.graphics.opengl.PalettedSpriteShader
import nz.net.ultraq.redhorizon.graphics.opengl.PalettedSpriteShader.PalettedSpriteShaderContext

import java.nio.ByteBuffer

/**
 * A texture used to swap index values to adjust the output colour from a
 * palette.  Used for performing palette-swapping animations, or to remap a set
 * of colours to another (eg: faction colours).
 *
 * @author Emanuel Rabina
 */
class PaletteSwapMap extends GraphicsNode<PaletteSwapMap, PalettedSpriteShaderContext>
	implements AutoCloseable {

	final Class<? extends Shader> shaderClass = PalettedSpriteShader
	int[] colours
	private boolean coloursChanged
	private final ByteBuffer buffer
	final Texture texture

	/**
	 * Constructor, builds an adjustment map for the given colours.
	 *
	 * TODO: Don't tie this to C&C and instead have the classic package provide an
	 *       implementation.  Also need a data structure that represents these
	 *       swaps a lot better.
	 */
	PaletteSwapMap(int[] colours) {

		this.colours = colours
		buffer = ByteBuffer.allocateNative(256)
		256.times { i ->
			if (i in 80..95) {
				buffer.put(colours[i - 80] as byte)
			}
			else {
				buffer.put(i as byte)
			}
		}
		buffer.flip()
		texture = new OpenGLTexture(256, 1, 1, buffer)
	}

	@Override
	void close() {

		texture.close()
	}

	@Override
	void render(PalettedSpriteShaderContext shaderContext) {

		shaderContext.setSwapMap(this)
		update()
	}

	/**
	 * Set the faction to use.  This will cause the adjustment map to be updated
	 * with the next call to {@link #update()}.
	 */
	void setColours(int[] colours) {

		this.colours = colours
		coloursChanged = true
	}

	/**
	 * Update the if needed, eg: for faction changes.
	 */
	void update() {

		if (coloursChanged) {
			(80..95).each { i ->
				buffer.put(i, colours[i - 80] as byte)
			}
			texture.update(buffer)
			coloursChanged = false
		}
	}
}
