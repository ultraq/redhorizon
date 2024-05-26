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
import nz.net.ultraq.redhorizon.classic.maps.UnitLine
import nz.net.ultraq.redhorizon.filetypes.FileExtensions

import java.nio.ByteBuffer
import java.util.regex.Pattern

/**
 * A plain-text configuration file with sections for grouping certain kinds of
 * data.  Used for maps, missions, and general configuration.  This object acts
 * like a map (for each section) of maps (for the section data).
 *
 * @author Emanuel Rabina
 */
@FileExtensions('ini')
class IniFile implements MapFile {

	private static final Pattern COMMENT_PATTERN = ~/^\s*;.*/
	private static final Pattern SECTION_PATTERN = ~/\[(.+)\](\s*;.*)?/
	private static final Pattern LINE_PATTERN = ~/([^=]+)=([^;]+)(;.*)?/

	private final Map<String, Map<String, String>> sections = [:]

	/**
	 * Constructor, build a new INI file from the given input stream.
	 *
	 * @param inputStream
	 */
	IniFile(InputStream inputStream) {

		def currentSection

		inputStream.withReader { reader ->
			reader.eachLine { line ->
				if (line) {
					if (line.matches(COMMENT_PATTERN)) {
						return
					}
					def sectionMatcher = SECTION_PATTERN.matcher(line)
					if (sectionMatcher.matches()) {
						def sectionHeader = sectionMatcher.group(1)
						currentSection = [:]
						sections << [(sectionHeader): currentSection]
					}
					else {
						def lineMatcher = LINE_PATTERN.matcher(line)
						if (lineMatcher.matches()) {
							def lineKey = lineMatcher.group(1).trim()
							def lineValue = lineMatcher.group(2).trim()
							currentSection << [(lineKey): lineValue]
						}
					}
				}
			}
		}
	}

	/**
	 * Retrieve the key/value map of data for the given section in the file.
	 *
	 * @param section
	 * @return
	 */
	Map<String, String> getAt(String section) {

		return sections[section]
	}

	@Override
	List<InfantryLine> getInfantryData() {

		return this['INFANTRY'].collect { index, lineData -> InfantryLine.parse(lineData) }
	}

	@Override
	ByteBuffer getMapPackData() {

		return mapDataToBytes(this['MapPack'], 6)
	}

	@Override
	MapSection getMapSection() {

		var mapData = this['Map']
		return new MapSection(mapData['Theater'], mapData['X'] as int, mapData['Y'] as int, mapData['Width'] as int, mapData['Height'] as int)
	}

	@Override
	String getName() {

		return this['Basic']['Name']
	}

	@Override
	ByteBuffer getOverlayPackData() {

		return mapDataToBytes(this['OverlayPack'], 2)
	}

	@Override
	Map<String, String> getTerrainData() {

		return this['TERRAIN']
	}

	@Override
	List<UnitLine> getUnitsData() {

		return this['UNITS'].collect { index, lineData -> UnitLine.parse(lineData) }
	}

	@Override
	Map<Integer, Integer> getWaypointsData() {

		return this['Waypoints'].collectEntries { index, cellCoord -> [index as int, cellCoord as int] }
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

	@Override
	String toString() {

		return "INI/Configuration file"
	}
}
