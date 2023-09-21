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

import org.joml.Vector2f

import groovy.transform.TupleConstructor

/**
 * A line in a map file that represents an infantry unit on the map.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class InfantryLine {

	final String faction
	final String type
	final float health
	final Vector2f coords
	final int cellPos
	final String action
	final float heading
	final String trigger

	/**
	 * Create an {@code InfantryLine} record from the infantry data in a map file.
	 *
	 * @param line
	 * @return
	 */
	static InfantryLine fromString(String line) {

		var lineParts = line.split(',')
		var triggerName = lineParts[7]

		return new InfantryLine(
			lineParts[0],
			lineParts[1],
			100 / 256 * Integer.parseInt(lineParts[2]) as float,
			Integer.parseInt(lineParts[3]).asCellCoords(),
			Integer.parseInt(lineParts[4]),
			lineParts[5],
			360 / 256 * Float.parseFloat(lineParts[6]) as float,
			triggerName != "None" ? triggerName : null
		)
	}
}
