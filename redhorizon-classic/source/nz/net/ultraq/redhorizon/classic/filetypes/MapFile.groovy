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

package nz.net.ultraq.redhorizon.classic.filetypes

import nz.net.ultraq.redhorizon.classic.maps.InfantryLine
import nz.net.ultraq.redhorizon.classic.maps.StructureLine
import nz.net.ultraq.redhorizon.classic.maps.UnitLine

import java.nio.ByteBuffer

/**
 * A C&C-specific version of the map file.
 *
 * @author Emanuel Rabina
 */
interface MapFile {

	/**
	 * Returns the {@code [Basic]} section of data.
	 */
	BasicSection getBasicSection()

	/**
	 * Returns the list of infantry data in the {@code [INFANTRY]} section.
	 */
	List<InfantryLine> getInfantryData()

	/**
	 * Returns the base64-encoded {@code [MapPack]} data as binary data.
	 */
	ByteBuffer getMapPackData()

	/**
	 * Returns the {@code [Map]} section of data.
	 */
	MapSection getMapSection()

	/**
	 * Returns the base64-encoded {@code [OverlayPack]} data as binary data.
	 */
	ByteBuffer getOverlayPackData()

	/**
	 * Returns the list of structure data in the {@code [STRUCTURES]} section.
	 */
	List<StructureLine> getStructuresData()

	/**
	 * Returns the list of cell coordinates to terrain object data in the
	 * {@code [TERRAIN]} section.
	 */
	Map<String, String> getTerrainData()

	/**
	 * Returns the list of unit data in the {@code [UNITS]} section.
	 */
	List<UnitLine> getUnitsData()

	/**
	 * Returns the mapping of waypoint numbers to map coordinates in the
	 * {@code [Waypoints]} section.
	 */
	Map<Integer, Integer> getWaypointsData()

	/**
	 * The {@code [Basic]} section of data.
	 */
	record BasicSection(String name) {}

	/**
	 * The {@code [Map]} section of data.
	 */
	record MapSection(String theater, int x, int y, int width, int height) {}
}
