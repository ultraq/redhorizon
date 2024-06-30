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

package nz.net.ultraq.redhorizon.classic.resources

import nz.net.ultraq.redhorizon.engine.graphics.SpriteMaterial

/**
 * A material specific to paletted sprites.
 *
 * @author Emanuel Rabina
 */
class PalettedSpriteMaterial extends SpriteMaterial {

	private static final int[] IDENTITY_MAP = 0..255

	int[] adjustmentMap = IDENTITY_MAP

	PalettedSpriteMaterial() {
	}

	/**
	 * Create a new material that is a shallow copy of {@code other}.
	 */
	PalettedSpriteMaterial(PalettedSpriteMaterial other) {

		super(other)
		adjustmentMap = other.adjustmentMap
	}
}
