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

package nz.net.ultraq.redhorizon.jomlextensions

import org.joml.primitives.Rayf

/**
 * Extension methods for {@link Rayf}.
 *
 * @author Emanuel Rabina
 */
class RayfExtensions {

	/**
	 * Convenience method for setting all of the ray's values at once.
	 */
	static Rayf set(Rayf self, float ox, float oy, float oz, float dx, float dy, float dz) {

		self.oX = ox
		self.oY = oy
		self.oZ = oz
		self.dX = dx
		self.dY = dy
		self.dZ = dz
		return self
	}
}
