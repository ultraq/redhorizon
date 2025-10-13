/*
 * Copyright 2020, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import nz.net.ultraq.redhorizon.classic.codecs.PackData
import nz.net.ultraq.redhorizon.classic.maps.InfantryLine
import nz.net.ultraq.redhorizon.classic.maps.StructureLine
import nz.net.ultraq.redhorizon.classic.maps.UnitLine

import java.nio.ByteBuffer
import java.util.regex.Pattern

/**
 * A plain-text configuration file with sections for grouping certain kinds of
 * data.  Used for maps, missions, and general configuration.  This object acts
 * like a map (for each section) of maps (for the section data).
 *
 * @author Emanuel Rabina
 */
class IniFile {

	private static final Pattern COMMENT_PATTERN = ~/^\s*;.*/
	private static final Pattern SECTION_PATTERN = ~/\[(.+)\](\s*;.*)?/
	private static final Pattern LINE_PATTERN = ~/([^=]+)=([^;]+)(;.*)?/

	private final Map<String, Map<String, String>> sections = [:]

	/**
	 * Constructor, read the INI file from the given input stream.
	 */
	IniFile(InputStream inputStream) {

		var currentSection
		inputStream.withReader { reader ->
			reader.eachLine { line ->
				if (line) {
					if (line.matches(COMMENT_PATTERN)) {
						return
					}
					var sectionMatcher = SECTION_PATTERN.matcher(line)
					if (sectionMatcher.matches()) {
						var sectionHeader = sectionMatcher.group(1)
						currentSection = [:]
						sections << [(sectionHeader): currentSection]
					}
					else {
						var lineMatcher = LINE_PATTERN.matcher(line)
						if (lineMatcher.matches()) {
							var lineKey = lineMatcher.group(1).trim()
							var lineValue = lineMatcher.group(2).trim()
							currentSection << [(lineKey): lineValue]
						}
					}
				}
			}
		}
	}

	/**
	 * Convert this INI file to a more-specific type.
	 */
	Object asType(Class clazz) {

		if (clazz == MapFile) {
			return new IniMapFile()
		}
		if (clazz == RulesFile) {
			return new IniRulesFile()
		}
		throw new IllegalArgumentException("Cannot convert IniFile to ${clazz}")
	}

	/**
	 * Retrieve the key/value map of data for the given section in the file.
	 */
	Map<String, String> getAt(String section) {

		return sections[section]
	}

	@Override
	String toString() {

		return "INI/Configuration file"
	}

	/**
	 * A map file backed by this INI file.
	 */
	private class IniMapFile implements MapFile {

		@Override
		BasicSection getBasicSection() {
			var basicData = sections['Basic']
			return new BasicSection(
				basicData['Name']
			)
		}

		@Override
		List<InfantryLine> getInfantryData() {
			return sections['INFANTRY'].collect { index, lineData -> InfantryLine.parse(lineData) }
		}

		@Override
		ByteBuffer getMapPackData() {
			return mapDataToBytes(sections['MapPack'], 6)
		}

		@Override
		MapSection getMapSection() {
			var mapData = sections['Map']
			return new MapSection(
				mapData['Theater'],
				mapData['X'] as int,
				mapData['Y'] as int,
				mapData['Width'] as int,
				mapData['Height'] as int
			)
		}

		@Override
		ByteBuffer getOverlayPackData() {
			return mapDataToBytes(sections['OverlayPack'], 2)
		}

		@Override
		List<StructureLine> getStructuresData() {
			return sections['STRUCTURES'].collect { index, lineData -> StructureLine.parse(lineData) }
		}

		@Override
		Map<String, String> getTerrainData() {
			return sections['TERRAIN']
		}

		@Override
		List<UnitLine> getUnitsData() {
			return sections['UNITS'].collect { index, lineData -> UnitLine.parse(lineData) }
		}

		@Override
		Map<Integer, Integer> getWaypointsData() {
			return sections['Waypoints'].collectEntries { index, cellCoord -> [index as int, cellCoord as int] }
		}

		/**
		 * Converts a map's character data into bytes that represent the tiles used
		 * throughout the map.
		 *
		 * @param data
		 *   A map section containing the character data to decode.
		 * @param chunks
		 *   Number of chunks to allocate the pack data during the decoding process.
		 * @return The converted map data.
		 */
		private static ByteBuffer mapDataToBytes(Map<String, String> data, int chunks) {

			// Turn the section into 8-bit chars
			def sourceBytes = ByteBuffer.allocateNative(data.size() * 70) // Lines are only ever 70 characters long
			(1..data.size()).each { i ->
				def line = data[i.toString()]
				line.chars.each { c ->
					sourceBytes.put(c as byte)
				}
			}
			sourceBytes.flip()

			// Decode section bytes
			return new PackData(chunks).decode(sourceBytes, ByteBuffer.allocateNative(49152)) // 128x128x3 bytes max
		}
	}

	/**
	 * A rules file backed by this INI file.
	 */
	private class IniRulesFile implements RulesFile {

		boolean asBoolean(String value) {
			return value == "yes" || value == "true"
		}

		@Override
		StructureConfig getStructureConfig(String name) {
			var unitData = sections[name]
			return new StructureConfig(
				unitData['Image'],
				asBoolean(unitData['Bib'])
			)
		}

		@Override
		UnitConfig getUnitConfig(String name) {
			var unitData = sections[name]
			return new UnitConfig(unitData['Image'])
		}
	}
}
