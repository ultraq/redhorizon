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
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsElement
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.filetypes.ImagesFile
import nz.net.ultraq.redhorizon.filetypes.Palette
import nz.net.ultraq.redhorizon.media.Image
import nz.net.ultraq.redhorizon.scenegraph.SelfVisitable

import org.joml.Rectanglef
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * The unit that gets displayed on then screen.
 * 
 * @author Emanuel Rabina
 */
class Unit implements GraphicsElement, SelfVisitable {

	private static final Logger logger = LoggerFactory.getLogger(Unit)

	private final Image[] bodyFrames
	private final Image[] turretFrames
	private final UnitAnimation[] unitAnimations

	private long animationTimeStart
	private final int frameRate = 15 // C&C ran at 15fps?
	private final GameTime gameTime

	float heading
	UnitAnimation animation

	/**
	 * Constructor, build a unit from the given data.
	 * 
	 * @param data
	 * @param imagesFile
	 * @param palette
	 * @param coordinates
	 * @param gameTime
	 */
	Unit(UnitData data, ImagesFile imagesFile, Palette palette, Rectanglef coordinates, GameTime gameTime) {

		def frameIndex = 0
		def buildFrames = { frames, part ->
			part.frames.times { time ->
				frames[time] = new Image(imagesFile.width, imagesFile.height, palette.format.value,
					imagesFile.imagesData[frameIndex++].applyPalette(palette), coordinates, false)
			}
		}

		def bodyPart = data.shpFile.parts.body
		bodyFrames = new Image[bodyPart.frames]
		buildFrames(bodyFrames, bodyPart)

		def turretPart = data.shpFile.parts.turret
		if (turretPart) {
			turretFrames = new Image[turretPart.frames]
			buildFrames(turretFrames, turretPart)
		}

		def animations = data.shpFile.animations
		if (animations) {
			unitAnimations = new UnitAnimation[animations.length]

			animations.eachWithIndex { animation, index ->
				def numAnimationFrames = animation.frames * animation.headings
				def animationFrames = new Image[numAnimationFrames]
				numAnimationFrames.times { time ->
					animationFrames[time] = new Image(imagesFile.width, imagesFile.height, palette.format.value,
						imagesFile.imagesData[frameIndex++].applyPalette(palette), coordinates, false)
				}
				unitAnimations[index] = new UnitAnimation(
					type: animation.type,
					framesPerHeading: animation.frames,
					headings: animation.headings,
					frames: animationFrames
				)
			}
		}

		this.gameTime = gameTime
	}

	@Override
	void delete(GraphicsRenderer renderer) {

		bodyFrames.each { frame ->
			frame.delete(renderer)
		}
		turretFrames.each { frame ->
			frame.delete(renderer)
		}
		unitAnimations.each { unitAnimation ->
			unitAnimation.frames.each { frame ->
				frame.delete(renderer)
			}
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
		unitAnimations.each { unitAnimation ->
			unitAnimation.frames.each { frame ->
				frame.init(renderer)
			}
		}
	}

	/**
	 * Selects this unit's next animation for rendering.
	 */
	void nextAnimation() {

		if (animation) {
			def currentAnimationIndex = unitAnimations.findIndexOf { unitAnimation ->
				unitAnimation.type == animation.type
			}
			def nextAnimationIndex = currentAnimationIndex + 1
			if (nextAnimationIndex == unitAnimations.length) {
				animation = null
			}
			else {
				animation = unitAnimations[nextAnimationIndex]
			}
		}
		else if (unitAnimations) {
			animation = unitAnimations.first()
		}

		logger.debug("${animation?.type ?: "No"} animation selected")
	}

	/**
	 * Selects this unit's previous animation for rendering.
	 */
	void previousAnimation() {

		if (animation) {
			def currentAnimationIndex = unitAnimations.findIndexOf { unitAnimation ->
				unitAnimation.type == animation.type
			}
			def previousAnimationIndex = currentAnimationIndex - 1
			if (previousAnimationIndex == -1) {
				animation = null
			}
			else {
				animation = unitAnimations[previousAnimationIndex]
			}
		}
		else if (unitAnimations) {
			animation = unitAnimations.last()
		}

		logger.debug("${animation?.type ?: "No"} animation selected")
	}

	@Override
	void render(GraphicsRenderer renderer) {

		if (animation) {
			def rotationFrames = -(heading / (360f / animation.headings)) as int
			def currentFrame = Math.floor((gameTime.currentTimeMillis - animationTimeStart) / 1000 * frameRate) % animation.framesPerHeading as int
			animation.frames[rotationFrames * animation.framesPerHeading + currentFrame].render(renderer)
		}
		else {
			def rotationFrame = -(heading / (360f / bodyFrames.length)) as int
			bodyFrames[rotationFrame].render(renderer)
			// TODO: Render the turret
		}
	}

	/**
	 * Adjust the heading of the unit such that it's rotated left enough to
	 * utilize its next frame or animation in that direction.
	 */
	void rotateLeft() {

		def degreesPerStep = (360f / (animation?.headings ?: bodyFrames.length)) as float
		heading = Math.wrap(heading - degreesPerStep as float, 0f, 360f)
	}

	/**
	 * Adjust the heading of the unit such that it's rotated right enough to
	 * utilize its next frame or animation in that direction.
	 */
	void rotateRight() {

		def degreesPerStep = (360f / (animation?.headings ?: bodyFrames.length)) as float
		heading = Math.wrap(heading + degreesPerStep as float, 0f, 360f)
	}

	private static class UnitAnimation {
		private String type
		private int framesPerHeading
		private int headings
		private Image[] frames
	}
}
