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

import nz.net.ultraq.redhorizon.classic.filetypes.PalFile
import nz.net.ultraq.redhorizon.filetypes.Palette

import picocli.CommandLine.Option

/**
 * A mixin of shareable palette options.
 *
 * @author Emanuel Rabina
 */
class PaletteOptions {

	@Option(
		names = ['--palette'],
		defaultValue = 'ra-temperate',
		description = 'Which game palette to apply to a paletted image.  One of ${COMPLETION-CANDIDATES}.  Defaults to ra-temperate',
		converter = PaletteTypeConverter,
		completionCandidates = { PaletteTypeConverter.COMPLETION_CANDIDATES }
	)
	PaletteType paletteType

	/**
	 * Load the palette indicated by the {@link #paletteType} CLI options.
	 *
	 * @param withAlphaMask
	 */
	Palette loadPalette(boolean withAlphaMask = false) {

		return getResourceAsStream(paletteType.file).withBufferedStream { inputStream ->
			def palette = new PalFile(inputStream)
			return withAlphaMask ? palette.withAlphaMask() : palette
		}
	}
}
