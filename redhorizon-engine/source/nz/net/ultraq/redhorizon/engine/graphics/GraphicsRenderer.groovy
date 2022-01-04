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

import nz.net.ultraq.redhorizon.events.EventTarget
import nz.net.ultraq.redhorizon.geometry.Dimension

import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.primitives.Rectanglef

import groovy.transform.NamedVariant
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import java.nio.ByteBuffer

/**
 * Interface for the graphics renderer, used by the graphics subsystem to draw
 * objects to the screen.
 * 
 * @author Emanuel Rabina
 */
interface GraphicsRenderer<TFramebuffer extends Framebuffer, TMaterial extends Material, TMesh extends Mesh,
	TShader extends Shader, TTexture extends Texture> extends EventTarget {

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
	 * Create a mesh that represents a line loop - a series of points where lines
	 * are drawn between them and then a final one is used to close the last and
	 * first points.
	 * 
	 * @param colour
	 * @param vertices
	 * @return
	 */
	TMesh createLineLoopMesh(Colour colour, Vector2f... vertices)

	/**
	 * Create a mesh representing disjoint lines.
	 * 
	 * @param colour
	 * @param vertices Every pair of vertices represents the start and end points
	 *                 of a line to be drawn.
	 * @return New lines mesh.
	 */
	TMesh createLinesMesh(Colour colour, Vector2f... vertices)

	/**
	 * Create a material out of the given component parts.
	 * 
	 * @param mesh
	 * @param texture
	 * @param shader
	 * @param transform
	 * @return
	 */
	@NamedVariant
	TMaterial createMaterial(TMesh mesh, TTexture texture, TShader shader, Matrix4f transform)

	/**
	 * Create a framebuffer that can be rendered to.
	 * 
	 * @param resolution
	 * @param filter
	 * @return
	 */
	TFramebuffer createFramebuffer(Dimension resolution, boolean filter)

	/**
	 * Create a new shader program for the shader source files with the given
	 * name.
	 * 
	 * @param name
	 * @param uniforms
	 * @return
	 */
	TShader createShader(String name, Uniform ...uniforms)

	/**
	 * Create a mesh to represent a surface onto which a texture will go, using
	 * the default texture coordinates.
	 * 
	 * @param surface
	 * @return
	 */
	default TMesh createSpriteMesh(Rectanglef surface) {

		return createSpriteMesh(surface, new Rectanglef(0, 0, 1, 1))
	}

	/**
	 * Create a mesh to represent a surface onto which a texture will go.
	 * 
	 * @param surface
	 * @param textureUVs
	 * @return
	 */
	TMesh createSpriteMesh(Rectanglef surface, Rectanglef textureUVs)

	/**
	 * Create and fill a texture with the given image data.
	 * 
	 * @param width
	 * @param height
	 * @param format
	 * @param data
	 * @return New texture object.
	 */
	TTexture createTexture(int width, int height, int format, ByteBuffer data)

	/**
	 * Delete framebuffer data.
	 * 
	 * @param framebuffer
	 */
	void deleteFramebuffer(TFramebuffer framebuffer)

	/**
	 * Delete all of the items tied to the material.
	 * 
	 * @param material
	 */
	void deleteMaterial(TMaterial material)

	/**
	 * Delete mesh data.
	 * 
	 * @param mesh
	 */
	void deleteMesh(TMesh mesh)

	/**
	 * Delete texture data.
	 * 
	 * @param texture
	 */
	void deleteTexture(TTexture texture)

	/**
	 * Render a material.
	 * 
	 * @param material
	 */
	void drawMaterial(TMaterial material)

	/**
	 * Set a framebuffer to be used as the target for subsequent draw calls.  Use
	 * {@code null} to set the render target as the screen.
	 * 
	 * @param framebuffer
	 */
	void setRenderTarget(TFramebuffer framebuffer)

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
	TMaterial withMaterialBundler(
		@ClosureParams(value = SimpleType, options = 'nz.net.ultraq.redhorizon.engine.graphics.MaterialBundler')
		Closure closure)
}
