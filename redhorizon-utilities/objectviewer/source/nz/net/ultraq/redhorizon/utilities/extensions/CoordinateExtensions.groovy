/* 
 * Copyright 2021, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.utilities.extensions

import org.joml.Rectanglef
import org.joml.Vector2f

/**
 * A collection of extension methods to JOML classes to help translating between
 * the old C&C coordinate system to the OpenGL one.
 * 
 * @author Emanuel Rabina
 */
class CoordinateExtensions {

	private static final int TILES_X = 128
	private static final int TILES_Y = 128
	private static final int TILE_WIDTH = 24
	private static final int TILE_HEIGHT = 24

	/**
	 * Convert a number representing a cell value into a cell coordinates.
	 * 
	 * @param self
	 * @return
	 */
	static Vector2f asCellCoords(Integer self) {

		return new Vector2f(self % TILES_Y, Math.ceil(self / TILES_Y) as float)
	}

	/**
	 * Convert a set of cell coordinates into world coordinates.
	 * 
	 * @param self
	 * @return
	 */
	static Vector2f asWorldCoords(Vector2f self) {

		return self.set(self.x - (TILES_X / 2), (TILES_Y / 2) - self.y as float).mul(TILE_WIDTH, TILE_HEIGHT)
	}

	/**
	 * Update the rectangle so that min/max values are valid, ie: if minX > maxX
	 * then minX <-> maxX, and same for the Y axis.
	 * 
	 * @param self
	 * @return
	 */
	static Rectanglef makeValid(Rectanglef self) {

		if (self.minX > self.maxX) {
			def minX = self.minX
			self.minX = self.maxX
			self.maxX = minX
		}
		if (self.minY > self.maxY) {
			def minY = self.minY
			self.minY = self.maxY
			self.maxY = minY
		}
		return self
	}
}
