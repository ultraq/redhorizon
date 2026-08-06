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

package nz.net.ultraq.redhorizon.jomlextensions

import org.joml.primitives.Rectanglef

/**
 * A read-only wrapper around a {@link Rectanglef}.
 *
 * @author Emanuel Rabina
 */
class Rectanglefc {

	@Delegate(includes = ['lengthX', 'lengthY'], interfaces = false)
	private final Rectanglef rectangle

	/**
	 * Constructor, wrap an existing rectangle.
	 */
	Rectanglefc(Rectanglef rectangle) {

		this.rectangle = rectangle
	}

	/**
	 * Return the maximum X value of the rectangle.
	 */
	float getMaxX() {

		return rectangle.maxX
	}

	/**
	 * Return the maximum Y value of the rectangle.
	 */
	float getMaxY() {

		return rectangle.maxY
	}

	/**
	 * Return the minimum X value of the rectangle.
	 */
	float getMinX() {

		return rectangle.minX
	}

	/**
	 * Return the minimum Y value of the rectangle.
	 */
	float getMinY() {

		return rectangle.minY
	}
}
