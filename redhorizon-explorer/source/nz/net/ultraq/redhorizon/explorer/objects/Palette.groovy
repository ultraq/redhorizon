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

import nz.net.ultraq.redhorizon.classic.filetypes.PalFile
import nz.net.ultraq.redhorizon.engine.graphics.Colour
import nz.net.ultraq.redhorizon.engine.graphics.MeshType
import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.engine.scenegraph.nodes.Primitive

import org.joml.Vector2f
import org.joml.primitives.Rectanglef

/**
 * For viewing a palette file.
 *
 * @author Emanuel Rabina
 */
class Palette extends Node<Palette> {

	private static final int SWATCH_WIDTH = 20
	private static final int SWATCH_HEIGHT = 20

	Palette(PalFile palFile) {

		for (var i = 0; i < palFile.size; i++) {
			var colour = palFile[i]
			var swatch = new Primitive(
				MeshType.TRIANGLES,
				new Colour(
					"Palette-${i}",
					(colour[0] & 0xff) / 256,
					(colour[1] & 0xff) / 256,
					(colour[2] & 0xff) / 256
				),
				new Rectanglef(0, 0, SWATCH_WIDTH, SWATCH_HEIGHT) as Vector2f[]
			)
			var offsetX = (i % 16) * SWATCH_WIDTH
			var offsetY = Math.floor(i / 16) * -SWATCH_HEIGHT
			swatch.transform.translate(offsetX, offsetY as float)
			addChild(swatch)
		}

		transform.translate(8 * -SWATCH_WIDTH, 8 * SWATCH_HEIGHT)
	}
}
