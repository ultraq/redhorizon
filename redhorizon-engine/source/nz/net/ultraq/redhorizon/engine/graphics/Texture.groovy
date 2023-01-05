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

import nz.net.ultraq.redhorizon.engine.geometry.Dimension

/**
 * Representation of a single texture to render.
 *
 * @author Emanuel Rabina
 */
abstract class Texture implements AutoCloseable {

	final int width
	final int height
	final Dimension size

	/**
	 * Constructor, create a new texture of the given dimensions.
	 *
	 * @param width
	 * @param height
	 */
	Texture(int width, int height) {

		this.width = width
		this.height = height
		size = new Dimension(width, height)
	}

	/**
	 * Enable the use of this texture for the next rendering commands at a given
	 * texture unit slot.
	 *
	 * @param textureUnit
	 *   The texture unit to bind this texture to.  If not specified, then the
	 *   currently active texture slot is used.
	 */
	abstract void bind(int textureUnit = -1)
}
