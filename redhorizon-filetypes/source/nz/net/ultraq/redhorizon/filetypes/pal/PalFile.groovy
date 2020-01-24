/*
 * Copyright 2007 Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.filetypes.pal

import nz.net.ultraq.redhorizon.filetypes.FileExtensions
import nz.net.ultraq.redhorizon.filetypes.VgaPalette
import static nz.net.ultraq.redhorizon.filetypes.ColourFormat.FORMAT_RGB

/**
 * Implementation of the PAL file, which is an array of 256 colours (in VGA
 * 6-bits-per-channel format).
 * 
 * @author Emanuel Rabina
 */
@FileExtensions('pal')
class PalFile extends VgaPalette {

	private static final int PALETTE_SIZE = 256

	/**
	 * Constructor, builds a PAL file from an input stream.
	 * 
	 * @param input
	 */
	PalFile(InputStream inputStream) {

		super(PALETTE_SIZE, FORMAT_RGB, inputStream)
	}
}
