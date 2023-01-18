/* 
 * Copyright 2023, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.classic.units

import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.Mesh

import org.joml.primitives.Rectanglef

/**
 * Common code for all of the unit body type parts.
 *
 * @author Emanuel Rabina
 */
abstract class UnitBodyBase extends UnitPart implements Rotatable {

	protected Mesh mesh

	/**
	 * Constructor, build a new main unit body.
	 *
	 * @param unit
	 * @param width
	 * @param height
	 * @param headings
	 * @param imagesData
	 */
	UnitBodyBase(Unit unit, int width, int height) {

		super(unit, width, height)
	}

	/**
	 * Constructor, build a new main unit body with a default state.
	 *
	 * @param unit
	 * @param width
	 * @param height
	 * @param headings
	 * @param frames
	 * @param frameOffset
	 */
	UnitBodyBase(Unit unit, int width, int height, int headings, int frames, int frameOffset) {

		super(unit, width, height)

		unit.states << new UnitState(unit, 'default', headings, frames, frameOffset)
	}

	@Override
	void delete(GraphicsRenderer renderer) {

		renderer.deleteMesh(mesh)
	}

	@Override
	void init(GraphicsRenderer renderer) {

		mesh = renderer.createSpriteMesh(
			surface: new Rectanglef(0, 0, width as float, height as float)
		)
	}

	/**
	 * Calculate which of the frames to use based on the current heading.
	 *
	 * @return
	 */
	protected int rotationFrame() {

		return heading ? headings - (heading / degreesPerHeading) : 0
	}
}
