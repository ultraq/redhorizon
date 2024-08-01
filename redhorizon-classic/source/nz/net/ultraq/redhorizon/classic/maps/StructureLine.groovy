/*
 * Copyright 2024, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import groovy.transform.TupleConstructor

/**
 * A line in the map file that represents a structure on the map.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class StructureLine implements ObjectLine {

	final String faction
	final String type
	final float health
	final Point coords
	final float heading
	final String trigger
	final boolean rebuildable
	final boolean sellable

	/**
	 * Create a {@code StructureLine} record from the structure data in a map
	 * file.
	 */
	static StructureLine parse(String line) {

		var lineParts = line.split(',')
		var triggerName = lineParts[5]

		return new StructureLine(
			lineParts[0],
			lineParts[1],
			100 / 256 * Integer.parseInt(lineParts[2]),
			new Point(Integer.parseInt(lineParts[3]).asCellCoords()),
			360 / 256 * Float.parseFloat(lineParts[4]) as float,
			triggerName != "None" ? triggerName : null,
			lineParts[6] == "1",
			lineParts[7] == "1"
		)
	}
}
