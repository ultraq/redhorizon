/*
 * Copyright 2025, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import org.joml.Vector2f
import org.joml.Vector3f

import groovy.transform.EqualsAndHashCode

/**
 * A vertex is a point in a mesh, and all of its accompanying data for use in
 * a vertex shader.
 *
 * <p>Currently, all vertex data is the same across all shaders, so the static
 * {@link #LAYOUT} can be used everywhere.  If we want to allow customer shaders
 * with different layouts, then that will need to change.
 *
 * @author Emanuel Rabina
 */
@EqualsAndHashCode
class Vertex {

	/**
	 * An attribute list that describes the layout of the data in a vertex.
	 */
	static Attribute[] LAYOUT = [
		new Attribute('position', 0, Vector3f.FLOATS),
//		new Attribute('colour', 1, Colour.FLOATS),
//		new Attribute('textureUVs', 2, Vector2f.FLOATS)
	]

	/**
	 * The number of {@code float}s used to represent a vertex.
	 */
	static final int FLOATS = LAYOUT.sum { it.sizeInFloats() } as int

	/**
	 * The number of {@code byte}s used to represent a vertex.
	 */
	static final int BYTES = FLOATS * Float.BYTES

	Vector3f position
	Colour colour
	Vector2f textureUVs

	/**
	 * Constructor, create a vertex with position, colour, and texture
	 * coordinates.
	 */
	Vertex(Vector3f position, Colour colour, Vector2f textureUVs = new Vector2f(0, 0)) {

		this.position = position
		this.colour = colour
		this.textureUVs = textureUVs
	}

	/**
	 * Convert this vertex into another type that can represent it.
	 */
	Object asType(Class clazz) {

		switch (clazz) {
			case float[] -> new float[]{
				position.x, position.y, position.z,
//				colour.r, colour.g, colour.b, colour.a,
//				textureUVs.x, textureUVs.y
			}
			default -> throw new IllegalArgumentException("Cannot convert Vertex to ${clazz}")
		}
	}

	/**
	 * Update the data in this vertex using data from another.
	 */
	void update(Vertex other) {

		position.set(other.position)
		if (colour != other.colour) {
			colour = other.colour // Immutable object - should it be modifiable?
		}
		textureUVs.set(other.textureUVs)
	}

	/**
	 * An attribute of a vertex describes a part of its data.  eg: a vertex has
	 * positional data so will have a position attribute.
	 */
	static record Attribute(String name, int location, int sizeInFloats) {}
}
