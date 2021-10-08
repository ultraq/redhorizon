/* 
 * Copyright 2020, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.cli.objectviewer.units

import nz.net.ultraq.redhorizon.engine.graphics.GraphicsElement
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.Material
import nz.net.ultraq.redhorizon.engine.graphics.Mesh
import nz.net.ultraq.redhorizon.engine.graphics.Texture
import nz.net.ultraq.redhorizon.filetypes.Palette

import org.joml.primitives.Rectanglef

import java.nio.ByteBuffer

/**
 * The base unit renderer for drawing simple static bodies.
 * 
 * @author Emanuel Rabina
 */
class UnitRenderer implements GraphicsElement {

	protected final String type
	protected final Unit unit
	protected final int headings
	protected final ByteBuffer[] imagesData
	protected final Palette palette
	protected final float degreesPerHeading

	protected Material material
	protected Mesh mesh
	protected Texture[] textures

	/**
	 * Constructor, create a unit renderer with the following frames.
	 * 
	 * @param type
	 * @param unit
	 * @param headings
	 * @param turretHeadings
	 * @param imagesData
	 * @param palette
	 */
	UnitRenderer(String type, Unit unit, int headings, ByteBuffer[] imagesData, Palette palette) {

		this.type = type
		this.unit = unit
		this.headings = headings
		this.imagesData = imagesData
		this.palette = palette

		degreesPerHeading = (360f / headings) as float
	}

	@Override
	void delete(GraphicsRenderer renderer) {

		renderer.deleteMesh(mesh)
		textures.each { texture ->
			renderer.deleteTexture(texture)
		}
	}

	@Override
	void init(GraphicsRenderer renderer) {

		mesh = renderer.createSpriteMesh(new Rectanglef(0, 0, unit.width, unit.height))
		textures = imagesData.collect { data ->
			return renderer.createTexture(unit.width, unit.height, palette.format.value,
				data.applyPalette(palette).flipVertical(unit.width, unit.height, palette.format)
			)
		}
		material = renderer.createMaterial(
			mesh: mesh,
			transform:  unit.transform
		)
	}

	@Override
	void render(GraphicsRenderer renderer) {

		material.texture = textures[rotationFrames()]
		renderer.drawMaterial(material)
	}

	/**
	 * Calculate which of the frames to use based on the current heading.
	 * 
	 * @return
	 */
	protected int rotationFrames() {

		return unit.heading ? headings - (unit.heading / degreesPerHeading) : 0
	}
}
