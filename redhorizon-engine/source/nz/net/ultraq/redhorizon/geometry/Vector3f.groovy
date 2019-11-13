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

import groovy.transform.EqualsAndHashCode
import groovy.transform.TupleConstructor

/**
 * A 3-tuple, representing any 3-dimensional value.
 * 
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
@EqualsAndHashCode
class Vector3f {

	static final Vector3f ZERO = new Vector3f()

	float x
	float y
	float z

	/**
	 * Use Groovy's type conversion for returning orientation as an array.  Useful
	 * for OpenGL/AL array functions.
	 * 
	 * @param clazz
	 * @return [x, y, z]
	 */
	Object asType(Class clazz) {

		if (clazz == Float[]) {
			return [x, y, z] as float[]
		}

		return new IllegalArgumentException("Cannot convert Vector3f to type ${clazz}")
	}

	/**
	 * Returns this vector represented as (x,y,z).
	 * 
	 * @return Vector3f(x,y,z).
	 */
	@Override
	String toString() {

		return "Vector3f(${x},${y},${z})"
	}
}
