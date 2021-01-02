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
	 * Return each point around the rectangle as a series of vectors.
	 * 
	 * @param self
	 * @return An array of 4 vectors, one for each x/y point around the rectangle.
	 */
	static Vector2f[] asPoints(Rectanglef self) {

		return [
			new Vector2f(self.minX, self.minY),
			new Vector2f(self.minX, self.maxY),
			new Vector2f(self.maxX, self.maxY),
			new Vector2f(self.maxX, self.minY)
		]
	}

	/**
	 * Return a vector's values as a {@code float[]}.
	 * 
	 * @param clazz
	 * @return
	 */
	static Object asType(Vector3f self, Class clazz) {

		if (clazz == float[]) {
			return [self.x, self.y, self.z]
		}
		throw new IllegalArgumentException("Cannot convert Vector3f to type ${clazz}")
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
}
