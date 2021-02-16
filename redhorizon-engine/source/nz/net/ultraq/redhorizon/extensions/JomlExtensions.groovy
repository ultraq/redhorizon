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

import org.joml.Matrix4f
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
	 * Return a matrix's values as a {@code float[]}.
	 * 
	 * @param self
	 * @param clazz
	 * @return
	 */
	static Object asType(Matrix4f self, Class clazz) {

		if (clazz == float[]) {
			return new float[]{
				self.m00(), self.m01(), self.m02(), self.m03(),
				self.m10(), self.m11(), self.m12(), self.m13(),
				self.m20(), self.m21(), self.m22(), self.m23(),
				self.m30(), self.m31(), self.m32(), self.m33()
			}
		}
		throw new IllegalArgumentException("Cannot convert Vector2f to type ${clazz}")
	}

	/**
	 * Return a vector's values as a {@code float[]}.
	 * 
	 * @param self
	 * @param clazz
	 * @return
	 */
	static Object asType(Vector2f self, Class clazz) {

		if (clazz == float[]) {
			return new float[]{ self.x, self.y }
		}
		throw new IllegalArgumentException("Cannot convert Vector2f to type ${clazz}")
	}

	/**
	 * Return a vector's values as a {@code float[]}.
	 * 
	 * @param self
	 * @param clazz
	 * @return
	 */
	static Object asType(Vector3f self, Class clazz) {

		if (clazz == float[]) {
			return new float[]{ self.x, self.y, self.z }
		}
		throw new IllegalArgumentException("Cannot convert Vector3f to type ${clazz}")
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
