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

import nz.net.ultraq.redhorizon.classic.filetypes.MixFile

import groovy.transform.EqualsAndHashCode
import groovy.transform.TupleConstructor

/**
 * Metadata for entries in a mix file.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor
@EqualsAndHashCode
class MixEntry implements Entry<MixEntry> {

	final MixFile mixFile
	final nz.net.ultraq.redhorizon.classic.filetypes.MixEntry mixEntry
	final String name
	final Class<?> fileClass
	final long size
	final boolean unknown
	final String description

	@Override
	int compareTo(MixEntry other) {

		return name == '..' ? -1 :
			other.name == '..' ? 1 :
				!unknown && other.unknown ? -1 :
					unknown && !other.unknown ? 1 :
						unknown && other.unknown ? 0 :
							name <=> other.name
	}

	@Override
	String getType() {

		return fileClass?.simpleName
	}
}
