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

package nz.net.ultraq.redhorizon.extensions

import nz.net.ultraq.redhorizon.filetypes.FileExtensions

import org.reflections.Reflections

/**
 * Extensions for strings that represent file names.
 * 
 * @author Emanuel Rabina
 */
class FileNameExtensions {

	/**
	 * Find the appropriate class for reading a file with the given name.
	 *
	 * @param self
	 * @return
	 */
	static Class<?> getFileClass(String self) {

		def suffix = self.substring(self.lastIndexOf('.') + 1)
		return new Reflections(
			'nz.net.ultraq.redhorizon.filetypes',
			'nz.net.ultraq.redhorizon.classic.filetypes'
		)
			.getTypesAnnotatedWith(FileExtensions)
			.find { type ->
				def annotation = type.getAnnotation(FileExtensions)
				return annotation.value().any { extension ->
					return extension.equalsIgnoreCase(suffix)
				}
			}
	}
}
