/* 
 * Copyright 2020, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.classic.units

import nz.net.ultraq.redhorizon.filetypes.ImagesFile
import nz.net.ultraq.redhorizon.filetypes.Palette

/**
 * An implementation of the rendered unit for infantry types.
 *
 * @author Emanuel Rabina
 */
class Infantry extends Unit {

	/**
	 * Constructor, build a unit from the given data.
	 *
	 * @param data
	 * @param imagesFile
	 * @param palette
	 */
	Infantry(UnitData data, ImagesFile imagesFile, Palette palette) {

		super(imagesFile, palette)
		var frameIndex = 0

		var bodyPart = data.shpFile.parts.body
		parts << new UnitBody(this, width, height, bodyPart.headings, 1, frameIndex)
		frameIndex += bodyPart.headings

		data.shpFile.states?.each { state ->
			states << new UnitState(this, state.name, state.headings, state.frames, frameIndex)
			frameIndex += (state.frames * state.headings)
		}
	}
}
