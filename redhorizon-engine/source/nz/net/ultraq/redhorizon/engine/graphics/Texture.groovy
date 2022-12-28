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

import groovy.transform.NamedVariant

/**
 * Representation of a single texture to render.
 * 
 * @author Emanuel Rabina
 */
class Texture {

	final int textureId

	final int width
	final int height
	final Dimension size

	/**
	 * Constructor, create a new texture of the given dimensions.
	 * 
	 * @param textureId
	 * @param width
	 * @param height
	 */
	@NamedVariant
	Texture(int textureId, int width, int height) {

		this.textureId = textureId
		this.width = width
		this.height = height
		size = new Dimension(width, height)
	}
}
