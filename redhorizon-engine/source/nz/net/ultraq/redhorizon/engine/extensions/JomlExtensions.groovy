/*
 * Copyright 2019, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine.extensions

import org.joml.FrustumIntersection
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.primitives.Rectanglef

/**
 * Extensions to JOMLs objects to work with Red Horizon.
 *
 * @author Emanuel Rabina
 */
class JomlExtensions {

	/**
	 * Return an array of points {@code Vector2f}s, each representing a point of
	 * this rectangle.
	 */
	static Object asType(Rectanglef self, Class clazz) {

		if (clazz == Vector2f[]) {
			return new Vector2f[]{
				new Vector2f(self.minX, self.minY),
				new Vector2f(self.minX, self.maxY),
				new Vector2f(self.maxX, self.maxY),
				new Vector2f(self.maxX, self.minY)
			}
		}
		throw new IllegalArgumentException("Cannot convert Rectanglef to type ${clazz}")
	}

	/**
	 * Calculate the scale factor for a rectangle to fit into the current one
	 * while maintaining its aspect ratio.
	 */
	static float calculateScaleToFit(Rectanglef self, Rectanglef other) {

		return Math.min(self.lengthX() / other.lengthX(), self.lengthY() / other.lengthY())
	}

	/**
	 * Update a rectangle's values so each point is equidistant from an 0,0 point
	 * as if on a plot.
	 */
	static Rectanglef center(Rectanglef self) {

		var halfLengthX = self.lengthX() / 2 as float
		var halfLengthY = self.lengthY() / 2 as float
		return self.set(-halfLengthX, -halfLengthY, halfLengthX, halfLengthY)
	}

	/**
	 * Expand the borders of a rectangle to include another rectangle.
	 */
	static Rectanglef expand(Rectanglef self, Rectanglef other) {

		return expand(self, other.minX, other.minY, other.maxX, other.maxY)
	}

	/**
	 * Expand the borders of a rectangle to include a given points representing
	 * another rectangle.
	 */
	static Rectanglef expand(Rectanglef self, float minX, float minY, float maxX, float maxY) {

		self.setMin(Math.min(self.minX, minX), Math.min(self.minY, minY))
		self.setMax(Math.max(self.maxX, maxX), Math.max(self.maxY, maxY))
		return self
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
	 * Test whether an XY plane represented by a rectangle is within this frustum.
	 */
	static boolean testPlaneXY(FrustumIntersection self, Rectanglef plane) {

		return self.testPlaneXY(plane.minX, plane.minY, plane.maxX, plane.maxY)
	}

	/**
	 * Translate this matrix by just an X and Y component.
	 */
	static Matrix4f translate(Matrix4f self, float x, float y) {

		return self.translate(x, y, 0)
	}
}
