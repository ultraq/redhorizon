/* 
 * Copyright 2013, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import nz.net.ultraq.redhorizon.geometry.Rectanglef

import java.nio.ByteBuffer

/**
 * Interface for the graphics renderer, used by the graphics subsystem to draw
 * objects to the screen.
 * 
 * @author Emanuel Rabina
 */
interface GraphicsRenderer {

	/**
	 * Create and fill a texture with the given image data.
	 * 
	 * @param data
	 * @param format
	 * @param width
	 * @param height
	 * @return New texture handle.
	 */
	int createTexture(ByteBuffer data, int format, int width, int height)

	/**
	 * Delete texture handles.
	 * 
	 * @param textureIds
	 */
	void deleteTextures(int... textureIds)

	/**
	 * Draw the texture over the given rectangle.
	 * 
	 * @param textureId
	 * @param rectangle
	 */
	void drawTexture(int textureId, Rectanglef rectangle)
}
