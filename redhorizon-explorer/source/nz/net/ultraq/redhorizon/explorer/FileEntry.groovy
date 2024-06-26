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

import groovy.transform.MapConstructor

/**
 * Metadata about a file or directory item.
 *
 * @author Emanuel Rabina
 */
@MapConstructor
class FileEntry implements Entry<FileEntry> {

	final File file
	final String name
	final String type

	@Override
	int compareTo(FileEntry other) {

		return name == '/..' || (file.directory && !other.file.directory) ? -1 :
			other.name == '/..' || (!file.directory && other.file.directory) ? 1 :
				file.name.toLowerCase() <=> other.file.name.toLowerCase()
	}

	@Override
	String getName() {

		return name ?: file.directory ? "/${file.name}" : file.name
	}

	@Override
	long getSize() {

		return file.size()
	}
}
