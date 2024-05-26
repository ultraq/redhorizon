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

package nz.net.ultraq.redhorizon.classic.maps

import nz.net.ultraq.redhorizon.engine.geometry.Point

/**
 * A line in a map file that represents an infantry unit on the map.
 *
 * @author Emanuel Rabina
 */
class InfantryLine extends UnitLine {

	final int cellPos

	InfantryLine(String faction, String type, float health, Point coords, int cellPos, String action, float heading, String trigger) {

		super(faction, type, health, coords, heading, action, trigger)
		this.cellPos = cellPos
	}

	/**
	 * Create an {@code InfantryLine} record from the infantry data in a map file.
	 */
	static InfantryLine parse(String line) {

		var lineParts = line.split(',')
		var triggerName = lineParts[7]

		return new InfantryLine(
			lineParts[0],
			lineParts[1],
			100 / 256 * Integer.parseInt(lineParts[2]) as float,
			new Point(Integer.parseInt(lineParts[3]).asCellCoords()),
			Integer.parseInt(lineParts[4]),
			lineParts[5],
			360 / 256 * Float.parseFloat(lineParts[6]) as float,
			triggerName != "None" ? triggerName : null
		)
	}
}
