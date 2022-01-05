/* 
 * Copyright 2022, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.explorer

import groovy.json.JsonSlurper

/**
 * A list of known classic C&C mix file entries so that names can be displayed
 * in the explorer.
 * 
 * @author Emanuel Rabina
 */
class MixDatabase {

	private static final String[] sources = ['ra-conquer.json']

	private final List<MixData> data = []

	/**
	 * Constructor, create a new database from the JSON data sources.
	 */
	MixDatabase() {

		def jsonSlurper = new JsonSlurper()
		sources
			.collect { source -> "${MixDatabase.packageName.replace('.', '/')}/mixdata/${source}" }
			.each { source ->
				def jsonData = getResourceAsStream(source).text
				jsonSlurper.parseText(jsonData).each { entry ->
					def entryId = entry['id']
					data << new MixData(
						id: (entryId instanceof String ? Long.decode(entryId) : entryId) as int,
						name: entry['name']
					)
				}
			}
	}

	/**
	 * Find the entry with the given ID.
	 * 
	 * @param id
	 * @return
	 */
	MixData find(int id) {

		return data.find { entry -> entry.id == id }
	}
}
