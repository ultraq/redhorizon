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

package nz.net.ultraq.redhorizon.classic.extensions

import nz.net.ultraq.redhorizon.engine.graphics.Material
import nz.net.ultraq.redhorizon.engine.graphics.Texture

/**
 * C&C classic extensions to the {@link Material} class.
 *
 * @author Emanuel Rabina
 */
class MaterialExtensions {

	private static final String KEY_ADJUSTMENTMAP = 'adjustmentMap'
	private static final String KEY_ALPHAMASK = 'alphaMask'
	private static final String KEY_PALETTE = 'palette'

	private static final int[] IDENTITY_MAP = 0..255

	static int[] getAdjustmentMap(Material self) {
		return self.attributes[KEY_ADJUSTMENTMAP] ?: IDENTITY_MAP
	}

	static Texture getAlphaMask(Material self) {
		return self.attributes[KEY_ALPHAMASK]
	}

	static Texture getPalette(Material self) {
		return self.attributes[KEY_PALETTE]
	}

	static void setAdjustmentMap(Material self, int[] adjustmentMap) {
		self.attributes[KEY_ADJUSTMENTMAP] = adjustmentMap
	}

	static void setAlphaMask(Material self, Texture alphaMask) {
		self.attributes[KEY_ALPHAMASK] = alphaMask
	}

	static void setPalette(Material self, Texture palette) {
		self.attributes[KEY_PALETTE] = palette
	}
}
