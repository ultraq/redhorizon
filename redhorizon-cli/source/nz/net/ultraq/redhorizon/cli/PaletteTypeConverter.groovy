/* 
 * Copyright 2021, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.cli

import nz.net.ultraq.redhorizon.classic.PaletteType

import picocli.CommandLine.ITypeConverter

/**
 * A mapping of the C&C palette types to arguments usable in the CLI tool.
 * 
 * @author Emanuel Rabina
 */
class PaletteTypeConverter implements ITypeConverter<PaletteType> {

	static final List<String> COMPLETION_CANDIDATES = PaletteType.collect { paletteType ->
		return paletteType.file.dropRight(4)
	}

	@Override
	PaletteType convert(String value) throws Exception {

		return PaletteType.find { paletteType ->
			return value == paletteType.file.dropRight(4)
		}
	}
}
