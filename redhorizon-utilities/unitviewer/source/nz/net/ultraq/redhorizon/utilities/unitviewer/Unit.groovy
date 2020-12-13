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

import nz.net.ultraq.redhorizon.classic.filetypes.mix.MixFile
import nz.net.ultraq.redhorizon.classic.filetypes.shp.ShpFile
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsElement
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.filetypes.Palette
import nz.net.ultraq.redhorizon.media.Image
import nz.net.ultraq.redhorizon.scenegraph.SelfVisitable

import org.joml.Rectanglef

/**
 * The unit that gets displayed on then screen.
 * 
 * @author Emanuel Rabina
 */
class Unit implements GraphicsElement, SelfVisitable {

	private final Image[] bodyFrames
	private final Image[] turretFrames

	/**
	 * Constructor, build a unit from the given data.
	 * 
	 * @param data
	 * @param palette
	 */
	Unit(UnitData data, Palette palette) {

		// TODO: Configure the path to the mix file, or do some kind of item lookup
		def mixFile = new MixFile(new File('mix/red-alert/Conquer.mix'))
		def mixFileEntry = mixFile.getEntry(data.shpFile.filename)
		def shpFile = mixFile.getEntryData(mixFileEntry).withStream { inputStream ->
			return new ShpFile(inputStream)
		}

		def frameIndex = 0
		def buildFrames = { frames, part ->
			part.frames.times { time ->
				frames[time] = new Image(shpFile.width, shpFile.height, palette.format.value,
					shpFile.imagesData[frameIndex++].applyPalette(palette),
					new Rectanglef(0, 0, shpFile.width * 2, shpFile.height * 2), false)
			}
		}

		def bodyPart = data.shpFile.parts.body
		bodyFrames = new Image[bodyPart.frames]
		buildFrames(bodyFrames, bodyPart)

		def turretPart = data.shpFile.parts.turret
		turretFrames = new Image[turretPart.frames]
		buildFrames(turretFrames, turretPart)
	}

	@Override
	void delete(GraphicsRenderer renderer) {

		bodyFrames.each { frame ->
			frame.delete(renderer)
		}
		turretFrames.each { frame ->
			frame.delete(renderer)
		}
	}

	@Override
	void init(GraphicsRenderer renderer) {

		bodyFrames.each { frame ->
			frame.init(renderer)
		}
		turretFrames.each { frame ->
			frame.init(renderer)
		}
	}

	@Override
	void render(GraphicsRenderer renderer) {

		bodyFrames[0].render(renderer)
		// TODO: Render the turret
	}
}
