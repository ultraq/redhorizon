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

package nz.net.ultraq.redhorizon.utilities.objectviewer.units

import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer

import java.nio.ByteBuffer

/**
 * Vehicle-specific unit renderer.
 * 
 * @author Emanuel Rabina
 */
class VehicleRenderer extends UnitRenderer {

	protected final int turretHeadings

	/**
	 * Constructor, create a vehicle renderer with the following frames.
	 *
	 * @param type
	 * @param unit
	 * @param headings
	 * @param turretHeadings
	 * @param imagesData
	 */
	VehicleRenderer(String type, Unit unit, int headings, int turretHeadings, ByteBuffer[] imagesData) {

		super(type, unit, headings, imagesData)
		this.turretHeadings = turretHeadings
	}

	@Override
	void render(GraphicsRenderer renderer) {

		super.render(renderer)
		if (turretHeadings) {
			material.texture = textures[headings + rotationFrames()]
			renderer.drawMaterial(material)
		}
	}
}
