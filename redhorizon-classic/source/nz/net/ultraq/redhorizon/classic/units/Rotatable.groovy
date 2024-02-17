/*
 * Copyright 2023, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.classic.units

/**
 * Trait for an independent rotation property indicating heading.  Note that
 * this is different from the {@code transform} rotation, and instead used to
 * control which sprite animation to display.
 *
 * @author Emanuel Rabina
 */
trait Rotatable {

	private float heading

	/**
	 * Return this object's heading.  The value will be within the range 0 - 360
	 * degrees.
	 *
	 * @return
	 */
	float getHeading() {

		return heading
	}

	/**
	 * Update this object's heading.  The value is clamped to the range 0 - 360
	 * degrees.
	 *
	 * @param newHeading
	 */
	void setHeading(float newHeading) {

		heading = Math.wrap(newHeading, 0f, 360f)
	}
}
