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

import org.joml.Rectanglef
import org.joml.Vector2f

import groovy.transform.MapConstructor

/**
 * A mesh defines the shape of an object, and so contain data on points and
 * edges.
 * 
 * @author Emanuel Rabina
 */
@MapConstructor
class Mesh {

	// Modern
	final int vertexArrayId
	final int vertexBufferId
	final int vertexType
	final int vertexCount
	final int elementBufferId
	final int elementType
	final int elementCount

	// Legacy
	final Colour colour
	final int primitiveType
	final Vector2f[] vertices
	final Rectanglef surface
	final float repeatX
	final float repeatY
}
