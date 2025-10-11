/*
 * Copyright 2007, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.classic.filetypes

import nz.net.ultraq.redhorizon.filetypes.FileExtensions

/**
 * Implementation of the PAL file, which is an array of 256 colours (in VGA
 * 6-bits-per-channel format).
 *
 * @author Emanuel Rabina
 */
@FileExtensions('pal')
class PalFile extends VgaPalette {

	/**
	 * Constructor, builds a PAL file from an input stream.
	 */
	PalFile(InputStream inputStream) {

		super(256, 3, inputStream)
	}

	@Override
	String toString() {

		return "PAL file, 256 colour VGA palette (6 bits per pixel)"
	}
}
