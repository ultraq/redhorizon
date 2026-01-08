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

package nz.net.ultraq.redhorizon.explorer.extensions

import nz.net.ultraq.redhorizon.audio.Music
import nz.net.ultraq.redhorizon.audio.Sound
import nz.net.ultraq.redhorizon.explorer.mixdata.MixData
import nz.net.ultraq.redhorizon.explorer.mixdata.RaMixEntry
import nz.net.ultraq.redhorizon.graphics.Animation
import nz.net.ultraq.redhorizon.graphics.Image
import nz.net.ultraq.redhorizon.graphics.Palette
import nz.net.ultraq.redhorizon.graphics.Video

/**
 * Add convenience methods/properties to objects that represent files.
 *
 * @author Emanuel Rabina
 */
class FileMappingExtensions {

	static final Map<String, Closure<Class>> FILE_EXTENSION_TO_CLASS = [
		'aud': { file ->
			// Let's say 1MB cutoff
			if ((file instanceof File && file.size() > 1024 * 1024) ||
				(file instanceof RaMixEntry && file.space() > 1024 * 1024)) {
				return Music
			}
			return Sound
		},
		'cps': { file -> Image },
		'pal': { file -> Palette },
		'pcx': { file -> Image },
		'vqa': { file -> Video },
		'wsa': { file -> Animation }
	]

	static final Map<String, String> FILE_EXTENSION_TO_NAME = [
		'aud': 'AUD sound file',
		'cps': 'CPS image file',
		'pal': 'PAL file',
		'pcx': 'PCX image file',
		'vqa': 'VQA video file',
		'wsa': 'WSA animation file'
	]

	/**
	 * Return a class that can handle the current file, or {@code null} if
	 * there is no implementation for it.
	 */
	static Class getSupportedFileClass(File self) {

		if (self) {
			var fileExtension = self.name.substring(self.name.lastIndexOf('.') + 1)
			var closure = FILE_EXTENSION_TO_CLASS[fileExtension]
			if (closure) {
				return closure(self)
			}
		}
		return null
	}

	/**
	 * Return a class that can handle the current RA-MIXer database entry, or
	 * {@code null} if there is no implementation for it.
	 */
	static Class getSupportedFileClass(RaMixEntry self) {

		if (self) {
			var fileExtension = self.name().substring(self.name().lastIndexOf('.') + 1)
			var closure = FILE_EXTENSION_TO_CLASS[fileExtension]
			if (closure) {
				return closure(self)
			}
		}
		return null
	}

	/**
	 * Return a class that can handle the current mix file entry, or {@code null}
	 * if there is no implementation for it.
	 */
	static Class getSupportedFileClass(MixData self) {

		if (self) {
			var fileExtension = self.name().substring(self.name().lastIndexOf('.') + 1)
			var closure = FILE_EXTENSION_TO_CLASS[fileExtension]
			if (closure) {
				return closure(self)
			}
		}
		return null
	}

	/**
	 * Return a name identifying the type of supported file, or {@code null} if
	 * there is no implementation for it.
	 */
	static String getSupportedFileName(File self) {

		return self ? FILE_EXTENSION_TO_NAME[self.name.substring(self.name.lastIndexOf('.') + 1)] : null
	}
}
