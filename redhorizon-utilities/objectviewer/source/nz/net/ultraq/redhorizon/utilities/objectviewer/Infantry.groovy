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

package nz.net.ultraq.redhorizon.utilities.objectviewer

import nz.net.ultraq.redhorizon.engine.GameTime
import nz.net.ultraq.redhorizon.filetypes.ImagesFile
import nz.net.ultraq.redhorizon.filetypes.Palette

import org.joml.Rectanglef

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
	 * @param coordinates
	 * @param gameTime
	 */
	Infantry(UnitData data, ImagesFile imagesFile, Palette palette, Rectanglef coordinates, GameTime gameTime) {

		def frameIndex = 0

		def bodyPart = data.shpFile.parts.body
		unitRenderers << new UnitRenderer('body', this, bodyPart.headings,
			buildImages(imagesFile, palette, coordinates, frameIndex..<(frameIndex += bodyPart.headings)))

		// TODO: Utilize alternative body frames for something
		def bodyAltPart = data.shpFile.parts.bodyAlt
		if (bodyAltPart) {
			frameIndex += bodyAltPart.headings
		}

		data.shpFile.animations?.each { animation ->
			unitRenderers << new UnitRendererAnimations(animation.type, this, animation.headings, animation.frames,
				buildImages(imagesFile, palette, coordinates, frameIndex..<(frameIndex += (animation.frames * animation.headings))),
				gameTime)
		}

		currentRenderer = unitRenderers.first()
	}
}
