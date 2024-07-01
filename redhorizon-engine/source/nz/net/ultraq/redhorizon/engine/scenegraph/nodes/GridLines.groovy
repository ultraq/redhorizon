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

package nz.net.ultraq.redhorizon.engine.scenegraph.nodes

import nz.net.ultraq.redhorizon.engine.graphics.Colour
import nz.net.ultraq.redhorizon.engine.graphics.MeshType
import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.engine.scenegraph.UpdateHint

import org.joml.Vector2f

/**
 * A set of grid lines to help with positioning of elements.
 *
 * @author Emanuel Rabina
 */
class GridLines extends Node<GridLines> {

	final UpdateHint updateHint = UpdateHint.NEVER

	/**
	 * Constructor, build a set of grid lines for the X and Y planes between the
	 * min/max values for every {@code step} rendered pixels.
	 */
	GridLines(int coordMin, int coordMax, int step) {

		var lines = new ArrayList<Vector2f>()
		for (var y = coordMin; y <= coordMax; y += step) {
			for (var x = coordMin; x <= coordMax; x += step) {
				if (!x && !y) {
					continue
				}
				lines.addAll(new Vector2f(x, y), new Vector2f(-x, y), new Vector2f(x, y), new Vector2f(x, -y))
			}
		}

		// TODO: Add support for vertices with different colours
		var cellLines = new Primitive(MeshType.LINES, new Colour('GridLines-Grey', 0.6, 0.6, 0.6), lines as Vector2f[])
		cellLines.name = 'Step lines'
		addChild(cellLines)

		var originLines = new Primitive(MeshType.LINES, new Colour('GridLines-DarkGrey', 0.2, 0.2, 0.2),
			new Vector2f(coordMin, 0), new Vector2f(coordMax, 0),
			new Vector2f(0, coordMin), new Vector2f(0, coordMax)
		)
		originLines.name = 'Origin lines'
		addChild(originLines)
	}
}
