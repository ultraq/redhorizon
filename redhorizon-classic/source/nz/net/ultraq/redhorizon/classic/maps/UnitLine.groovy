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
 * A line in a map file that represents a unit on the map.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class UnitLine implements ObjectLine {

	final String faction
	final String type
	final float health
	final Vector2f coords
	final float heading
	final String action
	final String trigger

	/**
	 * Create a {@code UnitLine} record from the unit data in a map file.
	 */
	static UnitLine parse(String line) {

		var lineParts = line.split(',')
		var triggerName = lineParts[6]

		return new UnitLine(
			lineParts[0],
			lineParts[1],
			100 / 256 * Integer.parseInt(lineParts[2]),
			Integer.parseInt(lineParts[3]).asCellCoords(),
			360 / 256 * Float.parseFloat(lineParts[4]) as float,
			lineParts[5],
			triggerName != "None" ? triggerName : null
		)
	}
}
