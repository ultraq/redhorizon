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

package nz.net.ultraq.redhorizon.engine.graphics

import nz.net.ultraq.redhorizon.engine.Entity
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.Mesh.Type
import nz.net.ultraq.redhorizon.graphics.Vertex

import org.joml.Vector3f
import org.joml.primitives.Rectanglef

/**
 * A set of grid lines to help with positioning of elements.
 *
 * @author Emanuel Rabina
 */
class GridLinesEntity extends Entity<GridLinesEntity> {

	/**
	 * Constructor, build a set of grid lines for the X and Y axes within the
	 * bounds specified by {@code range}, for every {@code step} rendered pixels.
	 */
	GridLinesEntity(Rectanglef range, float step, Colour originColour, Colour dividersColour) {

		// Alter values so that they line up with the origin
		var minX = Math.floor(range.minX / step) * step as int
		var maxX = Math.floor(range.maxX / step) * step as int
		var minY = Math.floor(range.minY / step) * step as int
		var maxY = Math.floor(range.maxY / step) * step as int

		var lines = new ArrayList<Vector3f>()
		for (float y = minY; y <= maxY; y += step) {
			for (float x = minX; x <= maxX; x += step) {
				if (!x && !y) {
					continue
				}
				lines.addAll(new Vector3f(x, y, 0), new Vector3f(-x, y, 0), new Vector3f(x, y, 0), new Vector3f(x, -y, 0))
			}
		}

		addComponent(new MeshComponent(Type.LINES, lines.collect { line ->
			return new Vertex(line, dividersColour)
		} as Vertex[])
			.withName('Dividers'))
		addComponent(new MeshComponent(Type.LINES, new Vertex[]{
			new Vertex(new Vector3f(range.minX, 0, 0), originColour),
			new Vertex(new Vector3f(range.maxX, 0, 0), originColour),
			new Vertex(new Vector3f(0, range.minX, 0), originColour),
			new Vertex(new Vector3f(0, range.maxX, 0), originColour)
		})
			.withName('Origin'))
	}
}
