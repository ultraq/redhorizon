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

package nz.net.ultraq.redhorizon.classic.filetypes.ini

import nz.net.ultraq.redhorizon.filetypes.FileExtensions
import nz.net.ultraq.redhorizon.filetypes.MapFile

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

	private static final Pattern SECTION_PATTERN = ~/\[(\w+)\](\s*;.*)?/
	private static final Pattern LINE_PATTERN = ~/([\w\d]+)=([^\s]+)(\s*;.*)?/

	private final Map<String,Map<String,String>> sections = [:]

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
					def sectionMatcher = SECTION_PATTERN.matcher(line)
					if (sectionMatcher.matches()) {
						def sectionHeader = sectionMatcher.group(1)
						currentSection = [:]
						sections << [(sectionHeader): currentSection]
					}
					else {
						def lineMatcher = LINE_PATTERN.matcher(line)
						if (lineMatcher.matches()) {
							def lineKey = lineMatcher.group(1)
							def lineValue = lineMatcher.group(2)
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
	Map<String,String> getAt(String section) {

		return sections[section]
	}
}
