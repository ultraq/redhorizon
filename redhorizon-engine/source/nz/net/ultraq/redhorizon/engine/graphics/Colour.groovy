/* 
 * Copyright 2020, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine.graphics

import groovy.transform.TupleConstructor

/**
 * Certain colour values for reference.
 * 
 * @author Emanuel Rabina
 */
@TupleConstructor
class Colour {

	/**
	 * The number of {@code byte}s used to represent a colour value.
	 */
	static final int BYTES = FLOATS * Float.BYTES

	/**
	 * The number of {@code float}s used to represent a colour value.
	 */
	static final int FLOATS = 4

	static final Colour BLACK  = new Colour('Black',  0, 0, 0)
	static final Colour BLUE   = new Colour('Blue',   0, 0, 1)
	static final Colour GREEN  = new Colour('Green',  0, 1, 0)
	static final Colour RED    = new Colour('Red',    1, 0, 0)
	static final Colour WHITE  = new Colour('White',  1, 1, 1)
	static final Colour YELLOW = new Colour('Yellow', 1, 1, 0)

	final String name
	final float r
	final float g
	final float b
	final float a = 1

	/**
	 * Convert this colour into another type that can represent it.
	 * 
	 * @param clazz
	 * @return
	 */
	Object asType(Class clazz) {

		if (clazz == float[]) {
			return new float[]{ r, g, b, a }
		}
		if (clazz == byte[]) {
			return new byte[]{ r * 255, g * 255, b * 255, a * 255 }
		}
		throw new IllegalArgumentException("Cannot convert Colour to ${clazz}")
	}

	/**
	 * Return this colour in the form: "Name (r, g, b, a)"
	 * 
	 * @return
	 */
	@Override
	String toString() {

		return "${name} (${r}, ${g}, ${b}, ${a})"
	}

	/**
	 * Return a new colour from a base one but with a specific alpha value.
	 * 
	 * @param alpha
	 * @return
	 */
	Colour withAlpha(float alpha) {

		return new Colour(this.name, this.r, this.g, this.b, alpha)
	}
}
