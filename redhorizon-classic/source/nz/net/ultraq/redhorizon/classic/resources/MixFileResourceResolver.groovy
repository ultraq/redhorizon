/*
 * Copyright 2026, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.classic.resources

import nz.net.ultraq.redhorizon.classic.filetypes.MixFile
import nz.net.ultraq.redhorizon.resources.ResourceResolver

import groovy.transform.TupleConstructor

/**
 * Resolve resources found in {@code mix} files.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class MixFileResourceResolver implements ResourceResolver {

	final MixFile mixFile

	@Override
	InputStream resolve(String path) {

		var mixEntry = mixFile.getEntry(path)
		return mixEntry ? mixFile.getEntryData(mixEntry) : null
	}
}
