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

import nz.net.ultraq.redhorizon.filetypes.ColourFormat

import org.joml.Vector2f
import org.joml.primitives.Rectanglef

import groovy.transform.ImmutableOptions
import java.nio.ByteBuffer
import java.util.concurrent.Future

/**
 * Interface to make requests of the graphics system.
 *
 * @author Emanuel Rabina
 */
interface GraphicsRequests {

	static interface Request<T extends GraphicsResource> {}

	static record ShaderRequest(String name) implements Request<Shader> {}

	@ImmutableOptions(knownImmutables = ['layout', 'colour'])
	static record MeshRequest(MeshType type, VertexBufferLayout layout, Colour colour, Vector2f[] vertices, int[] indices = null)
		implements Request<Mesh> {}

	@ImmutableOptions(knownImmutables = ['surface'])
	static record SpriteMeshRequest(Rectanglef surface) implements Request<Mesh> {}

	@ImmutableOptions(knownImmutables = ['data'])
	static record TextureRequest(int width, int height, ColourFormat format, ByteBuffer data) implements Request<Texture> {
	}

	/**
	 * Request the creation or retrieval of the given resource type from the
	 * graphics system, which will eventually be resolved in the returned
	 * {@code Future}.
	 */
	<V extends GraphicsResource, R extends Request<V>> Future<V> requestCreateOrGet(R request)

	/**
	 * Request the graphics system to delete a resource.
	 */
	void requestDelete(GraphicsResource... resources)
}
