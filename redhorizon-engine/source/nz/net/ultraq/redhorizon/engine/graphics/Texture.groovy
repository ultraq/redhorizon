/* 
 * Copyright 2021, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import java.nio.ByteBuffer

/**
 * Representation of a single texture, meant to be reused in the more complex
 * media types.
 * 
 * @author Emanuel Rabina
 */
class Texture implements GraphicsElement {

	final int width
	final int height
	final int format
	private ByteBuffer textureData
	final Boolean filter
	final float repeatX
	final float repeatY

	private Integer textureId

	/**
	 * Constructor, creates a texture from the given image data.
	 * 
	 * @param width
	 * @param height
	 * @param format
	 * @param textureData
	 * @param filter
	 * @param repeatX
	 * @param repeatY
	 */
	Texture(int width, int height, int format, ByteBuffer textureData, Boolean filter = null, float repeatX = 1, float repeatY = 1) {

		this.width       = width
		this.height      = height
		this.format      = format
		this.textureData = ByteBuffer.fromBuffersDirect(textureData)
		this.filter      = filter
		this.repeatX     = repeatX
		this.repeatY     = repeatY
	}

	@Override
	void delete(GraphicsRenderer renderer) {

		if (textureId != null) {
			renderer.deleteTextures(textureId)
			textureId = null
		}
	}

	/**
	 * Get the ID assigned by the renderer for this texture.
	 * 
	 * @return
	 */
	Integer getTextureId() {

		return textureId
	}

	@Override
	void init(GraphicsRenderer renderer) {

		if (textureId == null) {
			textureId = filter != null ?
				renderer.createTexture(textureData, format, width, height, filter) :
				renderer.createTexture(textureData, format, width, height)
			textureData = null // The texture data can be freed from system RAM as it exists in VRAM
		}
	}

	/**
	 * Does nothing, as a texture does not have a location at which to be drawn.
	 * Instead, the texture ID should be used by more complex media types that
	 * have position information.
	 * 
	 * @param renderer
	 */
	@Override
	void render(GraphicsRenderer renderer) {
	}
}
