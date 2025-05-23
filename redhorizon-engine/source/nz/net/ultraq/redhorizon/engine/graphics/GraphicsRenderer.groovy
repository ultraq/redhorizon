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
import nz.net.ultraq.redhorizon.engine.graphics.Mesh.MeshType
import nz.net.ultraq.redhorizon.events.EventTarget
import nz.net.ultraq.redhorizon.filetypes.ColourFormat

import org.joml.Matrix4fc
import org.joml.Vector2f
import org.joml.primitives.Rectanglef

import java.nio.Buffer
import java.nio.ByteBuffer

/**
 * Interface for the graphics renderer, used by the graphics subsystem to draw
 * objects to the screen.
 *
 * @author Emanuel Rabina
 */
interface GraphicsRenderer extends AutoCloseable, EventTarget {

	/**
	 * Clears the buffer.
	 */
	void clear()

	/**
	 * Create a framebuffer that can be rendered to.
	 */
	default Framebuffer createFramebuffer(Dimension resolution, boolean filter) {

		return createFramebuffer(resolution.width(), resolution.height(), filter)
	}

	/**
	 * Create a framebuffer that can be rendered to.
	 */
	Framebuffer createFramebuffer(int width, int height, boolean filter)

	/**
	 * Create a mesh without indices or textureUVs.
	 */
	default Mesh createMesh(MeshType type, VertexBufferLayout layout, Vector2f[] vertices, Colour colour) {

		return createMesh(type, layout, vertices, colour, null, false, null)
	}

	/**
	 * Create a mesh with all of the mesh parts.
	 */
	Mesh createMesh(MeshType type, VertexBufferLayout layout, Vector2f[] vertices, Colour colour, Vector2f[] textureUVs,
		boolean dynamic, int[] index)

	/**
	 * Create a new shader program from the given configuration, or return the
	 * existing shader program if one has already been created from the config.
	 */
	default Shader createShader(ShaderConfig config) {

		return createShader(config.name, config.vertexShaderSource, config.fragmentShaderSource, config.attributes, config.uniforms)
	}

	/**
	 * Create a new shader program from a pair of vertex and fragment shader
	 * scripts, or return the existing shader program if one has already been
	 * created with the given name.
	 */
	Shader createShader(String name, String vertexShaderSource, String fragmentShaderSource, Attribute[] attributes, Uniform[] uniforms)

	/**
	 * Create a mesh to represent a surface onto which a texture will go.  This is
	 * a convenience method for {@link #createMesh}.
	 */
	Mesh createSpriteMesh(Rectanglef surface)

	/**
	 * Create a mesh to represent a surface onto which a texture will go.  This is
	 * a convenience method for {@link #createMesh}.
	 */
	Mesh createSpriteMesh(Rectanglef surface, Rectanglef textureUVs)

	/**
	 * Create a sprite sheet from a series of image data.
	 */
	SpriteSheet createSpriteSheet(int width, int height, ColourFormat format, ByteBuffer[] data)

	/**
	 * Create and fill a texture with the given image data.
	 */
	Texture createTexture(int width, int height, ColourFormat format, ByteBuffer data)

	/**
	 * Create a shared uniform buffer filled with the given data.
	 */
	UniformBuffer createUniformBuffer(String name, Buffer data, boolean global)

	/**
	 * Delete a graphics resource.
	 */
	void delete(GraphicsResource resource)

	/**
	 * Draw a mesh using the given shader.
	 */
	void draw(Mesh mesh, Matrix4fc transform, Shader shader)

	/**
	 * Draw a mesh using a shader and material to configure the shader.
	 */
	void draw(Mesh mesh, Matrix4fc transform, Shader shader, Material material)

	/**
	 * Set a framebuffer to be used as the target for subsequent draw calls.  Use
	 * {@code null} to set the render target as the screen.
	 */
	void setRenderTarget(Framebuffer framebuffer)
}
