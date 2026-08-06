/*
 * Copyright 2025, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import org.joml.primitives.Rectanglei

/**
 * Extensions to the {@link Rectanglei} class.
 *
 * @author Emanuel Rabina
 */
class RectangleiExtensions {

	/**
	 * Set a rectangle to represent the given values.
	 */
	static Rectanglei set(Rectanglei self, int minX, int minY, int maxX, int maxY) {

		self.minX = minX
		self.minY = minY
		self.maxX = maxX
		self.maxY = maxY
		return self
	}

	/**
	 * Set the length parts of a rectangle, adjusting {@code maxX}/{@code maxY} so
	 * that they create the given lengths.
	 */
	static Rectanglei setLengths(Rectanglei self, int lengthX, int lengthY) {

		return self.setMax(self.minX + lengthX, self.minY + lengthY)
	}
}
