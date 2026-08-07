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

import org.joml.primitives.Circlef
import org.joml.primitives.Intersectionf
import org.joml.primitives.Rectanglef

/**
 * Extensions to the {@link Circlef} class.
 *
 * @author Emanuel Rabina
 */
class CirclefExtensions {

	/**
	 * Check if two circles intersect.
	 */
	static boolean intersects(Circlef self, Circlef other) {

		return Intersectionf.testCircleCircle(self.x, self.y, self.r, other.x, other.y, other.r)
	}

	/**
	 * Check if a circle intersects a rectangle.
	 */
	static boolean intersects(Circlef self, Rectanglef other) {

		return RectanglefExtensions.intersectsCircle(other, self)
	}
}
