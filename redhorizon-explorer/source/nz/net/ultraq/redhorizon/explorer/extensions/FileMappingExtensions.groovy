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

import nz.net.ultraq.redhorizon.explorer.mixdata.MixData
import nz.net.ultraq.redhorizon.explorer.mixdata.RaMixEntry

/**
 * Add convenience methods/properties to objects that represent files.
 *
 * @author Emanuel Rabina
 */
class FileMappingExtensions {

	// TODO: A lot of this mapping could be properties on the decoder classes?

	static final Map<String, String> FILE_EXTENSION_TO_TYPE = [
		'aud': 'AUD sound file',
		'cps': 'CPS image file',
		'int': 'INT tilemap file',
		'mix': 'MIX archive file',
		'pal': 'PAL palette file',
		'pcx': 'PCX image file',
		'shp': 'SHP sprite sheet file',
		'sno': 'SNO tilemap file',
		'tem': 'TEM tilemap file',
		'v00': 'AUD sound file',
		'v01': 'AUD sound file',
		'v02': 'AUD sound file',
		'v03': 'AUD sound file',
		'vqa': 'VQA video file',
		'wsa': 'WSA animation file'
	]

	/**
	 * Return a name identifying the type of supported file, or {@code null} if
	 * there is no implementation for it.
	 */
	static String guessedFileType(File self) {

		return self?.file ? FILE_EXTENSION_TO_TYPE[self.name.substring(self.name.lastIndexOf('.') + 1)] : null
	}

	/**
	 * Return a name identifying the type of supported file, or {@code null} if
	 * there is no implementation for it.
	 */
	static String guessedFileType(MixData self) {

		return self ? FILE_EXTENSION_TO_TYPE[self.name().substring(self.name().lastIndexOf('.') + 1)] : null
	}

	/**
	 * Return a name identifying the type of supported file, or {@code null} if
	 * there is no implementation for it.
	 */
	static String guessedFileType(RaMixEntry self) {

		return self ? FILE_EXTENSION_TO_TYPE[self.name().substring(self.name().lastIndexOf('.') + 1)] : null
	}
}
