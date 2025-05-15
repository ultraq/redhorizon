/*
 * Copyright 2025, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.runtime

import groovy.transform.TupleConstructor

/**
 * Reads the `version` from a properties file.  This works in conjunction with
 * the Red Horizon build scripts which expand `version` to be that from the
 * Gradle build scripts.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class VersionReader {

	final String propertiesFile

	/**
	 * Read the version value from the configured properties file name.
	 */
	String read() {

		return getResourceAsStream(propertiesFile).withBufferedReader { reader ->
			var cliProperties = new Properties()
			cliProperties.load(reader)
			var version = cliProperties.getProperty('version')
			return version == '${version}' ? '(development)' : version
		}
	}
}
