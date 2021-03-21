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

import nz.net.ultraq.redhorizon.filetypes.Palette

import org.joml.Matrix4f
import org.joml.Rectanglef
import org.joml.Vector2f

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import java.nio.ByteBuffer

/**
 * Interface for the graphics renderer, used by the graphics subsystem to draw
 * objects to the screen.
 * 
 * @author Emanuel Rabina
 */
interface GraphicsRenderer {

	/**
	 * Use the renderer in a batch rendering mode within the context of the given
	 * closure.  While the renderer will auto-flush when its buffers are full,
	 * callers will still need to do a final call to {@link BatchRenderer#flush}
	 * when they are done with it to ensure that all rendered objects are drawn to
	 * the screen.
	 * <p>
	 * The batch renderer is currently limited in some aspects in that the same
	 * shader and certain uniforms must be used across the batch.  These appear as
	 * parameters on this method.
	 * 
	 * @param shaderType
	 * @param closure
	 */
	void asBatchRenderer(ShaderType shaderType,
		@ClosureParams(value = SimpleType, options = 'nz.net.ultraq.redhorizon.engine.graphics.BatchRenderer') Closure closure)

	/**
	 * Clears the buffer.
	 */
	void clear()

	/**
	 * Create a camera with the given projection and view matrices.
	 * 
	 * @param view
	 * @param projection
	 */
	void createCamera(Matrix4f projection, Matrix4f view)

	/**
	 * Create a mesh that represents a line loop - a series of points where lines
	 * are drawn between them and then a final one is used to close the last and
	 * first points.
	 * 
	 * @param colour
	 * @param vertices
	 * @return
	 */
	Mesh createLineLoopMesh(Colour colour, Vector2f... vertices)

	/**
	 * Create a mesh representing disjoint lines.
	 * 
	 * @param colour
	 * @param vertices Every pair of vertices represents the start and end points
	 *                 of a line to be drawn.
	 * @return New lines mesh.
	 */
	Mesh createLinesMesh(Colour colour, Vector2f... vertices)

	/**
	 * Create a new material with all of its parts.
	 * 
	 * @param mesh
	 * @param texture
	 * @param shaderType
	 * @return
	 */
	Material createMaterial(Mesh mesh, Texture texture, ShaderType shaderType)

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
	 * Create a texture out of a palette.
	 * 
	 * @param palette
	 * @return New texture representing a palette.
	 */
	Texture createTexturePalette(Palette palette)

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
	 * Draw the material.
	 * 
	 * @param material
	 */
	void drawMaterial(Material material)

	/**
	 * Apply the given palette texture as the palette shader sampler uniform.
	 * 
	 * @param palette
	 */
	void setPalette(Texture palette)

	/**
	 * Update the camera's view matrix.
	 * 
	 * @param view
	 */
	void updateCamera(Matrix4f view)
}
