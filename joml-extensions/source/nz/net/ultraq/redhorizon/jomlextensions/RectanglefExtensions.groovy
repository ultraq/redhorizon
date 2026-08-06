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

import org.joml.Vector2f
import org.joml.primitives.Circlef
import org.joml.primitives.Rectanglef

/**
 * Extensions to the {@link Rectanglef} class.
 *
 * @author Emanuel Rabina
 */
class RectanglefExtensions {

	/**
	 * Convert a rectangle to any other supported type.
	 */
	static Object asType(Rectanglef self, Class<?> type) {

		if (type == Rectanglefc) {
			return new Rectanglefc(self)
		}
		return self
	}

	/**
	 * Update a rectangle's values so each point is equidistant from the origin
	 * (0, 0).
	 */
	static Rectanglef center(Rectanglef self) {

		var halfLengthX = self.lengthX() / 2f as float
		var halfLengthY = self.lengthY() / 2f as float
		return set(self, -halfLengthX, -halfLengthY, halfLengthX, halfLengthY)
	}

	/**
	 * Store and return the maxX/maxY components in the given vector.
	 */
	static Vector2f getMax(Rectanglef self, Vector2f result) {

		return result.set(self.maxX, self.maxY)
	}

	/**
	 * Store and return the minX/minY components in the given vector.
	 */
	static Vector2f getMin(Rectanglef self, Vector2f result) {

		return result.set(self.minX, self.minY)
	}

	/**
	 * Return whether a rectangle intersects a circle.
	 */
	static boolean intersectsCircle(Rectanglef self, Circlef other) {

		// A rectangle intersects a circle if the distance to the closest edge is
		// less than the circle's radius, or the rectangle contains the circle's center
		var distanceToClosestX = Math.min(Math.abs(other.x - self.minX), Math.abs(other.x - self.maxX))
		var distanceToClosestY = Math.min(Math.abs(other.y - self.minY), Math.abs(other.y - self.maxY))
		return distanceToClosestX < other.r || distanceToClosestY < other.r || self.containsPoint(other.x, other.y)
	}

	/**
	 * Set a rectangle to represent the given values.
	 */
	static Rectanglef set(Rectanglef self, float minX, float minY, float maxX, float maxY) {

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
	static Rectanglef setLengths(Rectanglef self, float lengthX, float lengthY) {

		return self.setMax(self.minX + lengthX as float, self.minY + lengthY as float)
	}
}
