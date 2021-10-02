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

package nz.net.ultraq.redhorizon.geometry

import org.joml.Rectanglef

import groovy.transform.Immutable

/**
 * A width/height value.
 * 
 * @author Emanuel Rabina
 */
@Immutable
class Dimension {

	final int width
	final int height

	/**
	 * Convert this object into another that can also represent width/height
	 * values.
	 * 
	 * @param clazz
	 * @return
	 */
	Object asType(Class clazz) {

		if (clazz == Rectanglef) {
			return new Rectanglef(0, 0, width, height)
		}
		else if (clazz == float[]) {
			return new float[] { width, height }
		}
		throw new IllegalArgumentException("Cannot convert Dimension to ${clazz}")
	}

	/**
	 * Return a new {@code Dimension} whose width/height values are multiplied by
	 * the given value.
	 * 
	 * @param right
	 * @return
	 */
	Dimension multiply(float right) {

		return new Dimension(width * right as int, height * right as int)
	}

	/**
	 * Return the width & height dimensions.
	 * 
	 * @return "(width)x(height)"
	 */
	@Override
	String toString() {

		return "${width}x${height}"
	}
}
