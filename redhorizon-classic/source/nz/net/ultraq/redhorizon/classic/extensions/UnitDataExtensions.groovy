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

package nz.net.ultraq.redhorizon.classic.extensions

import nz.net.ultraq.redhorizon.classic.units.UnitData

import groovy.json.JsonSlurper

/**
 * Extensions to retrieve unit data by object IDs.
 *
 * @author Emanuel Rabina
 */
class UnitDataExtensions {

	/**
	 * Return unit data as sourced from the JSON data.
	 */
	static UnitData getUnitData(Object self, String unitId) {

		return new JsonSlurper().parseText(getUnitDataJson(self, unitId)) as UnitData
	}

	/**
	 * Return unit data JSON.
	 */
	static String getUnitDataJson(Object self, String unitId) {

		return self.getResourceAsText("nz/net/ultraq/redhorizon/classic/units/data/${unitId.toLowerCase()}.json")
	}
}
