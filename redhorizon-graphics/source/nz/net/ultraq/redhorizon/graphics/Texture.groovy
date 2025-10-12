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

package nz.net.ultraq.redhorizon.graphics

import java.nio.ByteBuffer

/**
 * An image to draw on a mesh.
 *
 * @author Emanuel Rabina
 */
interface Texture extends GraphicsResource {

	/**
	 * Enable the use of this texture for the next rendering commands at the
	 * currently active texture slot.
	 */
	void bind()

	/**
	 * Enable the use of this texture for the next rendering commands at a given
	 * texture unit slot.
	 *
	 * @param textureUnit
	 *   The texture unit to bind this texture to.  If not specified, then the
	 *   currently active texture slot is used.
	 */
	void bind(int textureUnit)

	/**
	 * Return the height of the texture.
	 */
	int getHeight()

	/**
	 * Return the width of the texture.
	 */
	int getWidth()

	/**
	 * Update the texture data.
	 */
	void update(ByteBuffer newTextureData)
}
