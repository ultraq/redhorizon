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

import nz.net.ultraq.redhorizon.classic.filetypes.RaMixDatabase
import nz.net.ultraq.redhorizon.explorer.mixdata.MixData
import nz.net.ultraq.redhorizon.graphics.Animation
import nz.net.ultraq.redhorizon.graphics.Image
import nz.net.ultraq.redhorizon.graphics.Palette

/**
 * Add convenience methods/properties to objects that represent files.
 *
 * @author Emanuel Rabina
 */
class FileMappingExtensions {

	static final Map<String, Class> FILE_EXTENSION_TO_CLASS = [
		'cps': Image,
		'pal': Palette,
		'pcx': Image,
		'wsa': Animation
	]

	static final Map<String, String> FILE_EXTENSION_TO_NAME = [
		'cps': 'CPS image file',
		'pal': 'PAL file',
		'pcx': 'PCX image file',
		'wsa': 'WSA animation file'
	]

	/**
	 * Return a class that can handle the current file, or {@code null} if
	 * there is no implementation for it.
	 */
	static Class getSupportedFileClass(File self) {

		return self ? FILE_EXTENSION_TO_CLASS[self.name.substring(self.name.lastIndexOf('.') + 1)] : null
	}

	/**
	 * Return a class that can handle the current RA-MIXer database entry, or
	 * {@code null} if there is no implementation for it.
	 */
	static Class getSupportedFileClass(RaMixDatabase.Entry self) {

		return self ? FILE_EXTENSION_TO_CLASS[self.name().substring(self.name().lastIndexOf('.') + 1)] : null
	}

	/**
	 * Return a class that can handle the current mix file entry, or {@code null}
	 * if there is no implementation for it.
	 */
	static Class getSupportedFileClass(MixData self) {

		return self ? FILE_EXTENSION_TO_CLASS[self.name().substring(self.name().lastIndexOf('.') + 1)] : null
	}

	/**
	 * Return a name identifying the type of supported file, or {@code null} if
	 * there is no implementation for it.
	 */
	static String getSupportedFileName(File self) {

		return self ? FILE_EXTENSION_TO_NAME[self.name.substring(self.name.lastIndexOf('.') + 1)] : null
	}
}
