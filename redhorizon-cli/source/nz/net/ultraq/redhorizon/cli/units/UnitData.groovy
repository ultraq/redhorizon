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

package nz.net.ultraq.redhorizon.cli.units

/**
 * Model of the data of a C&C unit type.
 * 
 * @author Emanuel Rabina
 */
class UnitData {

	String type
	ShpFile shpFile

	static class ShpFile {
		ShpFileParts parts
		ShpFileAnimations[] animations
	}

	static class ShpFileParts {
		ShpFilePart body
		ShpFilePart bodyAlt
		ShpFilePart turret
	}

	static class ShpFilePart {
		int headings
	}

	static class ShpFileAnimations {
		String type
		int frames
		int headings
	}
}