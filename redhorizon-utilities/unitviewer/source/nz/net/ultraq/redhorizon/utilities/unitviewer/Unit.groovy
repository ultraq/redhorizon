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

import nz.net.ultraq.redhorizon.engine.graphics.GraphicsElement
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.filetypes.ImagesFile
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

	private final int rotationSteps
	private final float degreesPerStep
	private final Image[] bodyFrames
	private final Image[] turretFrames

	float heading

	/**
	 * Constructor, build a unit from the given data.
	 * 
	 * @param data
	 * @param imageData
	 * @param palette
	 * @param coordinates
	 */
	Unit(UnitData data, ImagesFile imageData, Palette palette, Rectanglef coordinates) {

		def frameIndex = 0
		def buildFrames = { frames, part ->
			part.frames.times { time ->
				frames[time] = new Image(imageData.width, imageData.height, palette.format.value,
					imageData.imagesData[frameIndex++].applyPalette(palette), coordinates, false)
			}
		}

		def bodyPart = data.shpFile.parts.body
		rotationSteps = bodyPart.frames
		degreesPerStep = (360f / rotationSteps) as float
		bodyFrames = new Image[bodyPart.frames]
		buildFrames(bodyFrames, bodyPart)

		def turretPart = data.shpFile.parts.turret
		if (turretPart) {
			turretFrames = new Image[turretPart.frames]
			buildFrames(turretFrames, turretPart)
		}
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

		def rotationFrame = -(heading / degreesPerStep) as int
		bodyFrames[rotationFrame].render(renderer)
		// TODO: Render the turret
	}

	/**
	 * Adjust the heading of the unit such that it's rotated left enough to
	 * utilize its next frame in that direction.
	 */
	void rotateLeft() {

		heading = Math.wrap(heading - degreesPerStep as float, 0f, 360f)
	}

	/**
	 * Adjust the heading of the unit such that it's rotated right enough to
	 * utilize its next frame in that direction.
	 */
	void rotateRight() {

		heading = Math.wrap(heading + degreesPerStep as float, 0f, 360f)
	}
}
