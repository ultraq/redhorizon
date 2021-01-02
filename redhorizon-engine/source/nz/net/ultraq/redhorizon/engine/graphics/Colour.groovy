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

	static final Colour BLACK  = new Colour(0, 0, 0)
	static final Colour BLUE   = new Colour(0, 0, 1)
	static final Colour GREEN  = new Colour(0, 1, 0)
	static final Colour RED    = new Colour(1, 0, 0)
	static final Colour WHITE  = new Colour(1, 1, 1)
	static final Colour YELLOW = new Colour(1, 1, 0)

	final float r
	final float g
	final float b
	final float a = 1

	/**
	 * Return a new colour from a base one but with a specific alpha value.
	 * 
	 * @param alpha
	 * @return
	 */
	Colour withAlpha(float alpha) {

		return new Colour(this.r, this.g, this.b, alpha)
	}
}
