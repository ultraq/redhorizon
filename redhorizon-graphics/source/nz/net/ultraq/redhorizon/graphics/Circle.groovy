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
 * A mesh object in the shape of a circle.
 *
 * @author Emanuel Rabina
 */
class Circle extends Shape {

	private static final float twoPi = Math.PI * 2 as float

	/**
	 * Constructor, produce a circle mesh.
	 */
	Circle(float radius, Colour colour, int segments = 16, boolean filled = false) {

		super(filled ? Type.TRIANGLE_FAN : Type.LINE_LOOP, createVertices(radius, colour, segments, filled))
	}

	/**
	 * Generate the vertices needed to create the requested circle.
	 */
	private static Vertex[] createVertices(float radius, Colour colour, int segments, boolean filled) {

		var vertices = []
		if (filled) {
			vertices << new Vertex(new Vector3f(0f, 0f, 0f), colour)
		}
		segments.times { i ->
			vertices << new Vertex(new Vector3f(
				radius * Math.cos(i * twoPi / segments) as float,
				radius * Math.sin(i * twoPi / segments) as float,
				0f
			), colour)
		}
		if (filled) {
			vertices << vertices[1] // To close out the fan
		}
		return vertices as Vertex[]
	}
}
