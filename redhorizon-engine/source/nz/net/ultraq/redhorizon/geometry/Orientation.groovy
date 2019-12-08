/* 
 * Copyright 2007, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.geometry

import org.joml.Vector3f

/**
 * The orientation of an object, consisting of a pair of 3-tuples: an 'up' and
 * an 'at'.
 * 
 * @author Emanuel Rabina
 */
class Orientation {

	Vector3f at
	Vector3f up

	/**
	 * Default constructor, creates an orientation looking down (0, 0, -1) with
	 * up being (0, 1, 0).
	 */
	Orientation() {

		at = new Vector3f(0, 0, -1)
		up = new Vector3f(0, 1, 0)
	}

	/**
	 * Use Groovy's type conversion for returning orientation as an array.  Useful
	 * for OpenGL/AL array functions.
	 * 
	 * @return [at.x, at.y, at.z, up.x, up.y, up.z]
	 */
	Object asType(Class clazz) {

		if (clazz == float[]) {
			return [at.x, at.y, at.z, up.x, up.y, up.z] as float[]
		}

		return new IllegalArgumentException("Cannot convert Orientation to type ${clazz}")
	}
}
