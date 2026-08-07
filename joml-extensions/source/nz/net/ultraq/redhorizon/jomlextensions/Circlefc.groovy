/*
 * Copyright 2026, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.jomlextensions

import org.joml.primitives.Circlef

/**
 * A read-only wrapper around a {@link Circlef}.
 *
 * @author Emanuel Rabina
 */
class Circlefc {

	private final Circlef circle

	/**
	 * Constructor, wrap an existing circle.
	 */
	Circlefc(Circlef circle) {

		this.circle = circle
	}

	/**
	 * Return the radius of the circle.
	 */
	float getR() {

		return circle.r
	}

	/**
	 * Return the X coordinate of the circle's center.
	 */
	float getX() {

		return circle.x
	}

	/**
	 * Return the Y coordinate of the circle's center.
	 */
	float getY() {

		return circle.y
	}
}
