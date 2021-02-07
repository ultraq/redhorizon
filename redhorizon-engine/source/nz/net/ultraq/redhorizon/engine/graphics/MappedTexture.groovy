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

import org.joml.Rectanglef

import groovy.transform.MapConstructor

/**
 * A variant of a {@code Texture} that references a parent texture and contains
 * a pre-built vertex buffer for faster drawing.
 * 
 * @author Emanuel Rabina
 */
@MapConstructor
class MappedTexture {

	final Texture parentTexture

	// Modern
	// TODO: Should we be storing this stuff in a 'mesh'?
	final int vertexArrayId
	final int bufferId
	final int elementBufferId

	// Legacy
	final Rectanglef surface
	final float repeatX
	final float repeatY
	final boolean flipVertical

	/**
	 * Shortcut to the instanced texture ID.
	 * 
	 * @return
	 */
	int getTextureId() {

		return parentTexture.textureId
	}
}
