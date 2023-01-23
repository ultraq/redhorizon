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

import nz.net.ultraq.redhorizon.classic.units.Faction
import nz.net.ultraq.redhorizon.engine.graphics.Material
import nz.net.ultraq.redhorizon.engine.graphics.Texture

/**
 * C&C classic extensions to the {@link Material} class.
 *
 * @author Emanuel Rabina
 */
class MaterialExtensions {

	private static final String FACTION_KEY = 'faction'
	private static final String PALETTE_KEY = 'palette'

	static Faction getFaction(Material self) {
		return self.attributes[FACTION_KEY] as Faction
	}

	static Texture getPalette(Material self) {
		return self.attributes[PALETTE_KEY] as Texture
	}

	static void setFaction(Material self, Faction faction) {
		self.attributes[FACTION_KEY] = faction
	}

	static void setPalette(Material self, Texture palette) {
		self.attributes[PALETTE_KEY] = palette
	}
}
