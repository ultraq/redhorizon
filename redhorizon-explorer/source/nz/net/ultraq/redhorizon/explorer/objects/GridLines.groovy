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

package nz.net.ultraq.redhorizon.explorer.objects

import nz.net.ultraq.redhorizon.engine.graphics.Colour
import nz.net.ultraq.redhorizon.engine.graphics.MeshType
import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.engine.scenegraph.nodes.Primitive

import org.joml.Vector2f

/**
 * An underlay of grid lines corresponding w/ the cell size of classic C&C
 * games.
 *
 * @author Emanuel Rabina
 */
class GridLines extends Node<GridLines> {

	private static final int COORD_MIN = -1536 // The max area a Red Alert map can be
	private static final int COORD_MAX = 1536

	GridLines() {

		var lines = new ArrayList<Vector2f>()
		for (var y = COORD_MIN; y <= COORD_MAX; y += 24) {
			for (var x = COORD_MIN; x <= COORD_MAX; x += 24) {
				if (!x && !y) {
					continue
				}
				lines.addAll(new Vector2f(x, y), new Vector2f(-x, y), new Vector2f(x, y), new Vector2f(x, -y))
			}
		}

		// TODO: Add support for vertices with different colours
		var cellLines = new Primitive(MeshType.LINES, new Colour('GridLines-Grey', 0.6, 0.6, 0.6), lines as Vector2f[])
		cellLines.name = "Cell lines"
		addChild(cellLines)

		var originLines = new Primitive(MeshType.LINES, new Colour('GridLines-DarkGrey', 0.2, 0.2, 0.2),
			new Vector2f(COORD_MIN, 0), new Vector2f(COORD_MAX, 0),
			new Vector2f(0, COORD_MIN), new Vector2f(0, COORD_MAX)
		)
		originLines.name = "Origin lines"
		addChild(originLines)
	}
}
