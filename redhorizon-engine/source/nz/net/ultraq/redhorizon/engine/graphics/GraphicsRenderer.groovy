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
	 * 
	 * @param closure
	 */
	void asBatchRenderer(
		@ClosureParams(value = SimpleType, options = 'nz.net.ultraq.redhorizon.engine.graphics.BatchRenderer')
		Closure closure)

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
	 * Create a material out of the given component parts.
	 * 
	 * @param mesh
	 * @param texture
	 * @param transform
	 * @return
	 */
	Material createMaterial(Mesh mesh, Texture texture, Matrix4f transform)

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
	 * Create a mesh to represent a surface onto which a texture will go, using
	 * the default texture coordinates.
	 * 
	 * @param surface
	 * @return
	 */
	Mesh createSpriteMesh(Rectanglef surface)

	/**
	 * Create a mesh to represent a surface onto which a texture will go.
	 * 
	 * @param surface
	 * @param textureUVs
	 * @return
	 */
	Mesh createSpriteMesh(Rectanglef surface, Rectanglef textureUVs)

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
	 * Render a material.
	 * 
	 * @param material
	 */
	void drawMaterial(Material material)

	/**
	 * Update the camera's view matrix.
	 * 
	 * @param view
	 */
	void updateCamera(Matrix4f view)

	/**
	 * Use a batching material builder within the context of the given closure
	 * that will return a single renderable material that is the sum of all the
	 * materials initialized within the closure.
	 * 
	 * @param closure
	 * @return
	 *   A material that represents all of the materials created within the
	 *   closure.  This material can then be rendered as normal to render all of
	 *   the created materials at once.
	 */
	Material withMaterialBundler(
		@ClosureParams(value = SimpleType, options = 'nz.net.ultraq.redhorizon.engine.graphics.MaterialBundler')
		Closure closure)
}
