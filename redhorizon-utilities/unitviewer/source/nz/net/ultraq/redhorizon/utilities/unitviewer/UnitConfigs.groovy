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

package nz.net.ultraq.redhorizon.utilities.unitviewer

/**
 * The set of units that I currently have configurations for.
 * 
 * @author Emanuel Rabina
 */
class UnitConfigs {

	private static final Map<String, String> CONFIGS = [
		'E1': 'e1.json',
		'2TNK': '2tnk.json',
		'4TNK': '4tnk.json',
		'HARV': 'harv.json',
		'JEEP': 'jeep.json'
	]

	/**
	 * Return the filename containing the configuration for the given unit.
	 * 
	 * @param name
	 * @return The name of the config file for the unit or {@code null} if there
	 *         is no configuration for that unit.
	 */
	static String getConfigFile(String name) {

		return CONFIGS[name.toUpperCase()]
	}
}
