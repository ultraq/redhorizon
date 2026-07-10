/*
 * Copyright 2026, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import nz.net.ultraq.redhorizon.graphics.Mesh.Type

import org.joml.Vector3f

/**
 * A mesh object in the shape of a rectangle.
 *
 * @author Emanuel Rabina
 */
class Rectangle extends Shape {

	private static final int[] fillIndex = [0, 1, 2, 2, 3, 0]

	/**
	 * Constructor, produce a rectangle with the given dimensions.
	 */
	Rectangle(float width, float height, Colour colour, boolean filled = false) {

		super(filled ? Type.TRIANGLES : Type.LINE_LOOP, new Vertex[]{
			new Vertex(new Vector3f(-width / 2f as float, -height / 2f as float, 0), colour),
			new Vertex(new Vector3f(width / 2f as float, -height / 2f as float, 0), colour),
			new Vertex(new Vector3f(width / 2f as float, height / 2f as float, 0), colour),
			new Vertex(new Vector3f(-width / 2f as float, height / 2f as float, 0), colour)
		}, filled ? fillIndex : null)
	}
}
