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

package nz.net.ultraq.redhorizon.engine.graphics

import org.joml.Vector2f

import groovy.transform.TupleConstructor

/**
 * A mesh defines the shape of an object, and so contain data on points and
 * edges.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
abstract class Mesh implements GraphicsResource {

	final int vertexType
	final Colour colour
	final Vector2f[] vertices
	final Vector2f[] textureUVs
	final int[] indices

	/**
	 * Use this mesh in upcoming render operations.
	 */
	abstract void bind()

	/**
	 * Update the textureUVs part of the mesh data.  This is only allowed on
	 * meshes that have been configured to use dynamic buffer data.
	 */
	abstract void updateTextureUvs(Vector2f[] textureUVs)
}
