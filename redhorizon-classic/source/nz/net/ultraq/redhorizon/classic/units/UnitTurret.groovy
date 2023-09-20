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

/**
 * An independently movable turret.
 */
class UnitTurret extends UnitBodyBase {

	private final int partOffset
	final String type = 'turret'

	/**
	 * Constructor, build a new main unit turret.
	 *
	 * @param unit
	 * @param width
	 * @param height
	 * @param headings
	 * @param frames
	 * @param frameOffset
	 */
	UnitTurret(Unit unit, int width, int height, int headings, int frames, int frameOffset, int partOffset) {

		super(unit, width, height, 'turret-default', headings, frames, frameOffset)
		this.partOffset = partOffset
	}

	@Override
	void render(GraphicsRenderer renderer) {

		unit.states[unit.stateIndex + partOffset].render(renderer)
	}
}
