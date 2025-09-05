/*
 * Copyright 2021, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.graphics

/**
 * A mesh defines the shape of an object, and so contain data on points and
 * edges.
 *
 * @author Emanuel Rabina
 */
interface Mesh extends GraphicsResource {

	/**
	 * Draw this mesh, using the currently-bound shader.
	 */
	void draw()

	/**
	 * Whether or not this mesh is allowed to be updated with new vertex data.
	 */
	boolean isDynamic()

	/**
	 * Update any vertex data in this mesh.  This is only allowed on meshes that
	 * have been configured to be dynamic (can query using {@link #isDynamic}).
	 */
	void updateVertexData(Vertex[] newVertices)

	/**
	 * The kind of primitive being constructed with the mesh.
	 */
	static enum Type {

		LINE_LOOP,
		LINES,
		TRIANGLES
	}
}
