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

package nz.net.ultraq.redhorizon.explorer.previews

import nz.net.ultraq.redhorizon.engine.Entity
import nz.net.ultraq.redhorizon.engine.graphics.MeshComponent
import nz.net.ultraq.redhorizon.engine.scripts.EntityScript
import nz.net.ultraq.redhorizon.engine.scripts.ScriptComponent
import nz.net.ultraq.redhorizon.explorer.ExplorerScene
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.Mesh.Type
import nz.net.ultraq.redhorizon.graphics.Palette
import nz.net.ultraq.redhorizon.graphics.Vertex

import org.joml.Vector2f
import org.joml.Vector3f

/**
 * For viewing a palette file.
 *
 * @author Emanuel Rabina
 */
class PalettePreview extends Entity<PalettePreview> {

	private static final int SWATCH_WIDTH = 24
	private static final int SWATCH_HEIGHT = 24
	private static final int[] index = new int[]{ 0, 1, 2, 2, 3, 0 }

	/**
	 * Constructor, create a preview by laying out all of the colours of the
	 * palette.
	 */
	PalettePreview(Palette palette) {

		palette.colourData.eachWithIndex { byte[] colour, int i ->
			var r = colour[0] & 0xff
			var g = colour[1] & 0xff
			var b = colour[2] & 0xff
			var offsetX = (i % 16) * SWATCH_WIDTH as float
			var offsetY = Math.floor(i / 16) * -SWATCH_HEIGHT as float
			addComponent(new MeshComponent(
				Type.TRIANGLES,
				new Vertex[]{
					new Vertex(new Vector3f(0, 0, 0), new Colour("Palette-${i}", r / 256, g / 256, b / 256), new Vector2f(0, 0)),
					new Vertex(new Vector3f(SWATCH_WIDTH, 0, 0), new Colour("Palette-${i}", r / 256, g / 256, b / 256), new Vector2f(1, 0)),
					new Vertex(new Vector3f(SWATCH_WIDTH, SWATCH_HEIGHT, 0), new Colour("Palette-${i}", r / 256, g / 256, b / 256), new Vector2f(1, 1)),
					new Vertex(new Vector3f(0, SWATCH_HEIGHT, 0), new Colour("Palette-${i}", r / 256, g / 256, b / 256), new Vector2f(0, 1))
				},
				index
			)
				.translate(offsetX, offsetY))
		}

		translate(8 * -SWATCH_WIDTH, 7 * SWATCH_HEIGHT)

		addComponent(new ScriptComponent(PalettePreviewScript))
	}

	static class PalettePreviewScript extends EntityScript<PalettePreview> implements AutoCloseable {

		private ExplorerScene scene

		@Override
		void close() {

			scene.gridLines.enable()
		}

		@Override
		void init() {

			scene = entity.scene as ExplorerScene
			scene.gridLines.disable()
		}
	}
}
