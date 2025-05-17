/*
 * Copyright 2024, Emanuel Rabina (http://www.ultraq.net.nz/)
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
import nz.net.ultraq.redhorizon.filetypes.ColourFormat

import org.joml.Vector2f
import org.joml.primitives.Rectanglef

import groovy.transform.ImmutableOptions
import java.nio.Buffer
import java.nio.ByteBuffer
import java.util.concurrent.CompletableFuture

/**
 * Interface to make requests of the graphics system.
 *
 * @author Emanuel Rabina
 */
interface GraphicsRequests {

	static interface Request<T extends GraphicsResource> {}

	@ImmutableOptions(knownImmutables = ['dimension'])
	static record FramebufferRequest(Dimension dimension, boolean filter) implements Request<Framebuffer> {}

	@ImmutableOptions(knownImmutables = ['shaderConfig'])
	static record ShaderRequest(ShaderConfig shaderConfig) implements Request<Shader> {}

	@ImmutableOptions(knownImmutables = ['layout', 'colour'])
	static record MeshRequest(MeshType type, VertexBufferLayout layout, Vector2f[] vertices, Colour colour,
		Vector2f[] textureUVs, boolean dynamic, int[] index) implements Request<Mesh> {

		MeshRequest(MeshType type, VertexBufferLayout layout, Vector2f[] vertices, Colour colour, boolean dynamic, int[] index) {
			this(type, layout, vertices, colour, null, dynamic, index)
		}

		MeshRequest(MeshType type, VertexBufferLayout layout, Vector2f[] vertices, Colour colour, boolean dynamic) {
			this(type, layout, vertices, colour, null, dynamic, null)
		}
	}

	@ImmutableOptions(knownImmutables = ['surface', 'textureUVs'])
	static record SpriteMeshRequest(Rectanglef surface, Rectanglef textureUVs) implements Request<Mesh> {
		SpriteMeshRequest(Rectanglef surface) {
			this(surface, new Rectanglef(0, 0, 1, 1))
		}
	}

	@ImmutableOptions(knownImmutables = ['data'])
	static record TextureRequest(int width, int height, ColourFormat format, ByteBuffer data) implements Request<Texture> {
	}

	static record SpriteSheetRequest(int width, int height, ColourFormat format, ByteBuffer... data) implements Request<SpriteSheet> {
	}

	@ImmutableOptions(knownImmutables = ['data'])
	static record UniformBufferRequest(String name, Buffer data, boolean global) implements Request<UniformBuffer> {
		UniformBufferRequest(String name, Buffer data) {
			this(name, data, false)
		}
	}

	/**
	 * Request the creation or retrieval of the given resource type from the
	 * graphics system, which will eventually be resolved in the returned
	 * {@code Future}.
	 */
	<V extends GraphicsResource, R extends Request<V>> CompletableFuture<V> requestCreateOrGet(R request)

	/**
	 * Request the graphics system to delete a resource.
	 */
	CompletableFuture<Void> requestDelete(GraphicsResource... resources)
}
