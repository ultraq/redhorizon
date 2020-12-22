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

package nz.net.ultraq.redhorizon.utilities.unitviewer

import nz.net.ultraq.redhorizon.engine.GameTime
import nz.net.ultraq.redhorizon.filetypes.ImagesFile
import nz.net.ultraq.redhorizon.filetypes.Palette

import org.joml.Rectanglef

/**
 * A rendered unit for building/structure types.
 * 
 * @author Emanuel Rabina
 */
class Structure extends Unit {

	/**
	 * Constructor, builds a structure out of unit data.
	 *
	 * @param data
	 * @param imagesFile
	 * @param palette
	 * @param coordinates
	 * @param gameTime
	 */
	Structure(UnitData data, ImagesFile imagesFile, Palette palette, Rectanglef coordinates, GameTime gameTime) {

		def frameIndex = 0
		def bodyPart = data.shpFile.parts.body

		["", "-damaged"].forEach { status ->
			unitRenderers << new UnitRenderer("body${status}", this, bodyPart.headings,
				buildImages(imagesFile, palette, coordinates, frameIndex..<(frameIndex += bodyPart.headings)))

			data.shpFile.animations?.each { animation ->
				unitRenderers << new UnitRendererAnimations(animation.type + status, this, animation.headings, animation.frames,
					buildImages(imagesFile, palette, coordinates, frameIndex..<(frameIndex += (animation.frames * animation.headings))),
					gameTime)
			}
		}

		currentRenderer = unitRenderers.first()
	}
}
