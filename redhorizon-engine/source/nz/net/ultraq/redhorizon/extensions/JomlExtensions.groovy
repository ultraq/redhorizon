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

package nz.net.ultraq.redhorizon.extensions

import org.joml.Rectanglef
import org.joml.Vector2f
import org.joml.Vector3f

/**
 * Extensions to JOMLs objects to work with Red Horizon.
 * 
 * @author Emanuel Rabina
 */
class JomlExtensions {

	/**
	 * Return an array of points {@code Vector2f}s, each representing a point of
	 * this rectangle.
	 * 
	 * @param self
	 * @param clazz
	 * @return An array of 4 vectors, one for each x/y point around the rectangle.
	 */
	static Object asType(Rectanglef self, Class clazz) {

		if (clazz == Vector2f[]) {
			return new Vector2f[] {
				new Vector2f(self.minX, self.minY),
				new Vector2f(self.minX, self.maxY),
				new Vector2f(self.maxX, self.maxY),
				new Vector2f(self.maxX, self.minY)
			}
		}
		throw new IllegalArgumentException("Cannot convert Rectanglef to type ${clazz}")
	}

	/**
	 * Center the rectangle about the origin.
	 * 
	 * @param self
	 * @return This rectangle.
	 */
	static Rectanglef center(Rectanglef self) {

		if (self.minX !== 0 || self.minY !== 0) {
			self.translate(-self.minX, -self.minY)
		}
		return self.translate(-self.maxX / 2 as int, -self.maxY / 2 as int)
	}

	/**
	 * Overload the {@code -} operator to perform vector subtraction.  Note that
	 * this creates a new object to store the result and is returned.
	 * 
	 * @param self
	 * @param v
	 * @return
	 */
	static Vector3f minus(Vector3f self, Vector3f v) {

		return self.sub(v, new Vector3f())
	}

	/**
	 * Overload the {@code +} operator to perform vector addition.  Note that
	 * this creates a new object to store the result and is returned.
	 * 
	 * @param self
	 * @param v
	 * @return
	 */
	static Vector3f plus(Vector3f self, Vector3f v) {

		return self.add(v, new Vector3f())
	}
}
