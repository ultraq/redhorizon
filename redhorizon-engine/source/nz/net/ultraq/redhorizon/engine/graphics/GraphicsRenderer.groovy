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

import nz.net.ultraq.redhorizon.engine.geometry.Dimension
import nz.net.ultraq.redhorizon.events.EventTarget
import nz.net.ultraq.redhorizon.filetypes.ColourFormat

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
interface GraphicsRenderer extends EventTarget {

	/**
	 * Clears the buffer.
	 */
	void clear()

	/**
	 * Create a framebuffer that can be rendered to.
	 *
	 * @param resolution
	 * @param filter
	 * @return
	 */
	default Framebuffer createFramebuffer(Dimension resolution, boolean filter) {

		return createFramebuffer(resolution.width, resolution.height, filter)
	}

	/**
	 * Create a framebuffer that can be rendered to.
	 *
	 * @param width
	 * @param height
	 * @param filter
	 * @return
	 */
	Framebuffer createFramebuffer(int width, int height, boolean filter)

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
	Material createMaterial(Texture texture, Matrix4f transform)

	/**
	 * Create a mesh with all of the mesh parts.
	 *
	 * @param type
	 * @param layout
	 * @param colour
	 * @param vertices
	 * @param textureUVs
	 * @param indices
	 * @return
	 */
	@NamedVariant
	Mesh createMesh(MeshType type, VertexBufferLayout layout, Colour colour, Vector2f[] vertices, Vector2f[] textureUVs,
		int[] indices)

	/**
	 * Create a new shader program from the given configuration, or return the
	 * existing shader program if one has already been created from the config.
	 *
	 * @param config
	 * @return
	 */
	Shader createShader(ShaderConfig config)

	/**
	 * Create a new shader program from a pair of vertex and fragment shader
	 * scripts, or return the existing shader program if one has already been
	 * created with the given name.
	 *
	 * @param name
	 * @param vertexShaderSource
	 * @param fragmentShaderSource
	 * @param uniforms
	 * @return
	 */
	Shader createShader(String name, String vertexShaderSource, String fragmentShaderSource, Uniform... uniforms)

	/**
	 * Create a mesh to represent a surface onto which a texture will go.  This is
	 * a convenience method for {@link #createMesh}.
	 *
	 * @param surface
	 * @param textureUVs
	 * @return
	 */
	@NamedVariant
	Mesh createSpriteMesh(Rectanglef surface, Rectanglef textureUVs)

	/**
	 * Create and fill a texture with the given image data.
	 *
	 * @param width
	 * @param height
	 * @param format
	 * @param data
	 * @return New texture object.
	 */
	Texture createTexture(int width, int height, ColourFormat format, ByteBuffer data)

	/**
	 * Delete framebuffer data.
	 *
	 * @param framebuffer
	 */
	void deleteFramebuffer(Framebuffer framebuffer)

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
	 * Draw a mesh using a shader and material to configure the shader.
	 *
	 * @param mesh
	 * @param shader
	 * @param material
	 */
	@NamedVariant
	void draw(Mesh mesh, Shader shader, Material material)

	/**
	 * Return the maximum size that any dimension of a texture can be for the
	 * current platform.
	 *
	 * @return
	 */
	int getMaxTextureSize()

	/**
	 * Set a framebuffer to be used as the target for subsequent draw calls.  Use
	 * {@code null} to set the render target as the screen.
	 *
	 * @param framebuffer
	 */
	void setRenderTarget(Framebuffer framebuffer)

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
	Tuple2<Mesh, Material> withMaterialBundler(
		@ClosureParams(value = SimpleType, options = 'nz.net.ultraq.redhorizon.engine.graphics.MaterialBundler')
			Closure closure)
}
