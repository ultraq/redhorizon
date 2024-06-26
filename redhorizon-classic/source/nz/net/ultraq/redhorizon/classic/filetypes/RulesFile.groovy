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

package nz.net.ultraq.redhorizon.classic.filetypes

import groovy.transform.TupleConstructor

/**
 * Interface for working with the Red Alert {@code rules.ini} file.
 *
 * @author Emanuel Rabina
 */
interface RulesFile {

	/**
	 * Returns the section of the rules file as a structure configuration record.
	 */
	StructureConfig getStructureConfig(String name)

	/**
	 * Returns the section of the rules file as a unit configuration record.
	 */
	UnitConfig getUnitConfig(String name)

	record StructureConfig(String image, boolean bib) {}

	// TODO: There seems to be some bug here when using a record and a single null
	//       value.  Once we get a second value, try a record again.
	@TupleConstructor(defaults = false)
	class UnitConfig {
		final String image
	}
}
