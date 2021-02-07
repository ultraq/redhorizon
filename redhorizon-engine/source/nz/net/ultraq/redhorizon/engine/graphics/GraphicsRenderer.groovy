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

import org.joml.Matrix4f
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
	 * Create a camera with the given projection matrix.
	 * 
	 * @param projection
	 */
	void createCamera(Matrix4f projection)

	/**
	 * Create an object representing line primitives so that they can be drawn
	 * later.
	 * 
	 * @param colour
	 * @param vertices Every pair of vertices represents the start and end points
	 *                 of a line to be drawn.
	 * @return New lines object.
	 */
	Lines createLines(Colour colour, Vector2f... vertices)

	/**
	 * Create a new material from a mesh and texture.
	 * 
	 * @param mesh
	 * @param texture
	 * @return
	 */
	Material createMaterial(Mesh mesh, Texture texture)

	/**
	 * Create a mesh to represent a surface onto which a texture will go.
	 * 
	 * @param surface
	 * @return
	 */
	Mesh createSpriteMesh(Rectanglef surface)

	/**
	 * Create a mesh to represent a surface onto which a texture will go.
	 * 
	 * @param surface
	 * @param repeatX
	 *   Number of times to repeat the texture on the X axis
	 * @param repeatY
	 *   Number of times to repeat the texture on the Y axis
	 * @return
	 */
	Mesh createSpriteMesh(Rectanglef surface, float repeatX, float repeatY)

	/**
	 * Create and fill a texture with the given image data.
	 * 
	 * @param data
	 * @param format
	 * @param width
	 * @param height
	 * @return New texture object.
	 */
	Texture createTexture(ByteBuffer data, int format, int width, int height)

	/**
	 * Create and fill a texture with the given image data.
	 * 
	 * @param width
	 * @param height
	 * @param format
	 * @param data
	 * @param filter Specify nearest-neighbouer filtering on the texture,
	 *               independent of the graphics configuration.
	 * @return New texture object.
	 */
	Texture createTexture(ByteBuffer data, int format, int width, int height, boolean filter)

	/**
	 * Delete the line data.
	 * 
	 * @param lines
	 */
	void deleteLines(Lines lines)

	/**
	 * Delete all of the items tied to the material.
	 * 
	 * @param material
	 */
	void deleteMaterial(Material material)

	/**
	 * Delete mesh data.
	 * 
	 * @param mesh
	 */
	void deleteMesh(Mesh mesh)

	/**
	 * Delete texture data.
	 * 
	 * @param texture
	 */
	void deleteTexture(Texture texture)

	/**
	 * Draw a coloured line that closes itself.
	 * 
	 * @param colour
	 * @param vertices
	 */
	void drawLineLoop(Colour colour, Vector2f... vertices)

	/**
	 * Draw any number of coloured lines.
	 * 
	 * @param lines
	 */
	void drawLines(Lines lines)

	/**
	 * Draw the material.
	 * 
	 * @param material
	 */
	void drawMaterial(Material material)

	/**
	 * Update the camera projection.
	 * 
	 * @param position
	 */
	void updateCamera(Vector3f position)
}
