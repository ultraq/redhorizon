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

import org.joml.Rectanglef
import org.joml.Vector2f
import org.joml.Vector3f

import java.nio.ByteBuffer

/**
 * Interface for the graphics renderer, used by the graphics subsystem to draw
 * objects to the screen.
 * 
 * @author Emanuel Rabina
 */
interface GraphicsRenderer {

	/**
	 * Clears the buffer.
	 */
	void clear()

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
	 * Create and fill a texture with the given image data.
	 * 
	 * @param data
	 * @param format
	 * @param width
	 * @param height
	 * @param filter Specify nearest-neighbouer filtering on the texture,
	 *               independent of the graphics configuration.
	 * @return New texture handle.
	 */
	int createTexture(ByteBuffer data, int format, int width, int height, boolean filter)

	/**
	 * Delete texture handles.
	 * 
	 * @param textureIds
	 */
	void deleteTextures(int... textureIds)

	/**
	 * Draw a coloured line.
	 * 
	 * @param colour
	 * @param vertexA
	 * @param vertexB
	 */
	void drawLine(Colour colour, Vector2f vertexA, Vector2f vertexB)

	/**
	 * Draw a coloured line that closes itself.
	 * 
	 * @param colour
	 * @param vertices
	 */
	void drawLineLoop(Colour colour, Vector2f... vertices)

	/**
	 * Draw the texture over the given rectangle.
	 * 
	 * @param textureId
	 * @param rectangle
	 */
	void drawTexture(int textureId, Rectanglef rectangle)

	/**
	 * Draw the texture over the given rectangle.
	 * 
	 * @param textureId
	 * @param rectangle
	 * @param repeatX
	 *   Number of times to repeat the texture on the X axis
	 * @param repeatY
	 *   Number of times to repeat the texture on the Y axis
	 * @param flipVertical
	 *   Whether or not to flip the texture on its vertical axis to compensate for
	 *   image data often having Y-coords in the opposite way to the rendering
	 *   coordinates.
	 */
	void drawTexture(int textureId, Rectanglef rectangle, float repeatX, float repeatY, boolean flipVertical)

	/**
	 * Update the camera projection.
	 * 
	 * @param position
	 */
	void updateCamera(Vector3f position)
}
