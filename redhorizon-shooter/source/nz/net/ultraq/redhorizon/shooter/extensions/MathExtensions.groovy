/*
 * Copyright 2025, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.shooter.extensions

/**
 * Shooter-specific extensions to the Math class.
 *
 * @author Emanuel Rabina
 */
class MathExtensions {

	/**
	 * Wrap a value within the 0-360 value range, good for representing some value
	 * in a circle.
	 */
	static float wrapToCircle(Math self, float value) {

		return Math.wrap(value, 0f, 360f)
	}
}
