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

import java.nio.ByteBuffer

/**
 * An implementation of a rendered unit for vehicle types.
 *
 * @author Emanuel Rabina
 */
class Vehicle extends Unit {

	/**
	 * Constructor, build a unit from the given data.
	 *
	 * @param data
	 * @param imagesFile
	 * @param palette
	 */
	Vehicle(UnitData data, ImagesFile imagesFile, Palette palette) {

		super(imagesFile.width, imagesFile.height)
		def frameIndex = 0

		def bodyPart = data.shpFile.parts.body
		def turretPart = data.shpFile.parts.turret
		unitRenderers << new VehicleRenderer('body', this, bodyPart.headings, turretPart?.headings ?: 0,
			imagesFile.imagesData[frameIndex..<(frameIndex += bodyPart.headings)] +
				(turretPart ? imagesFile.imagesData[frameIndex..<(frameIndex += turretPart.headings)] : [])
				as ByteBuffer[], palette)

		data.shpFile.animations?.each { animation ->
			unitRenderers << new UnitRendererAnimations(animation.type, this, animation.headings, animation.frames,
				imagesFile.imagesData[frameIndex..<(frameIndex += (animation.frames * animation.headings))] as ByteBuffer[],
				palette)
		}

		currentRenderer = unitRenderers.first()
	}
}
