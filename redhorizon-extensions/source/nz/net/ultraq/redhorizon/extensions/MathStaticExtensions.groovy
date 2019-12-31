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

/**
 * Static method extensions to the {@code Math} class.
 * 
 * @author Emanuel Rabina
 */
class MathStaticExtensions {

	/**
	 * Clamp a {@code short} value to the given range.
	 * 
	 * @param self
	 * @param value
	 * @param lower
	 * @param upper
	 * @return
	 */
	static short clamp(Math self, short value, short lower, short upper) {

		return (short)Math.min((short)Math.max(lower, value), upper)
	}

	/**
	 * Clamp an {@code int} value to the given range.
	 * 
	 * @param self
	 * @param value
	 * @param lower
	 * @param upper
	 * @return
	 */
	static int clamp(Math self, int value, int lower, int upper) {

		return Math.min(Math.max(lower, value), upper)
	}

	/**
	 * Clamp a {@code float} value to the given range.
	 * 
	 * @param self
	 * @param value
	 * @param lower
	 * @param upper
	 * @return
	 */
	static float clamp(Math self, float value, float lower, float upper) {

		return Math.min(Math.max(lower, value), upper)
	}
}
