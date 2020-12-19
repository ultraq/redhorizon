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

	private final List<UnitRenderer> unitRenderers = []
	private UnitRenderer currentRenderer
	private float heading

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
		def buildFrames = { int take ->
			def frames = new Image[take]
			take.times { i ->
				frames[i] = new Image(imagesFile.width, imagesFile.height, palette.format.value,
					imagesFile.imagesData[frameIndex++].applyPalette(palette), coordinates)
			}
			return frames
		}

		def bodyPart = data.shpFile.parts.body
		def turretPart = data.shpFile.parts.turret
		currentRenderer = new UnitRenderer("body", this, bodyPart.headings, turretPart?.headings ?: 0,
			buildFrames(bodyPart.headings) + (turretPart ? buildFrames(turretPart.headings) : []) as Image[])
		unitRenderers << currentRenderer

		def animations = data.shpFile.animations
		if (animations) {
			animations.each { animation ->
				unitRenderers << new UnitAnimationRenderer(animation.type, this, animation.headings, animation.frames,
					buildFrames(animation.frames * animation.headings), gameTime)
			}
		}
	}

	@Override
	void delete(GraphicsRenderer renderer) {

		unitRenderers.each { unitRenderer ->
			unitRenderer.delete(renderer)
		}
	}

	@Override
	void init(GraphicsRenderer renderer) {

		unitRenderers.each { unitRenderer ->
			unitRenderer.init(renderer)
		}
	}

	/**
	 * Selects this unit's next animation for rendering.
	 */
	void nextAnimation() {

		selectAnimation(+1)
	}

	/**
	 * Selects this unit's previous animation for rendering.
	 */
	void previousAnimation() {

		selectAnimation(-1)
	}

	@Override
	void render(GraphicsRenderer renderer) {

		currentRenderer.render(renderer)
	}

	/**
	 * Adjust the heading of the unit such that it's rotated left enough to
	 * utilize its next frame or animation in that direction.
	 */
	void rotateLeft() {

		heading = Math.wrap(heading - currentRenderer.degreesPerHeading as float, 0f, 360f)
	}

	/**
	 * Adjust the heading of the unit such that it's rotated right enough to
	 * utilize its next frame or animation in that direction.
	 */
	void rotateRight() {

		heading = Math.wrap(heading + currentRenderer.degreesPerHeading as float, 0f, 360f)
	}

	/**
	 * Select the next animation either next or previously in the sequence.
	 * 
	 * @param next
	 */
	private void selectAnimation(int next) {

		currentRenderer = unitRenderers[(unitRenderers.indexOf(currentRenderer) + next) % unitRenderers.size()]
		if (currentRenderer instanceof UnitAnimationRenderer) {
			currentRenderer.start()
		}
		logger.debug("${currentRenderer.type} animation selected")
	}

	/**
	 * Renderer for knowing what kind of body to draw.
	 */
	private static class UnitRenderer implements GraphicsElement {

		protected final String type
		protected final Unit unit
		protected final int headings
		protected final int turretHeadings
		protected final Image[] frames
		protected final float degreesPerHeading

		protected UnitRenderer(String type, Unit unit, int headings, int turretHeadings, Image[] frames) {

			this.type = type
			this.unit = unit
			this.headings = headings
			this.turretHeadings = turretHeadings
			this.frames = frames

			degreesPerHeading = (360f / headings) as float
		}

		@Override
		void delete(GraphicsRenderer renderer) {

			frames.each { frame ->
				frame.delete(renderer)
			}
		}

		@Override
		void init(GraphicsRenderer renderer) {

			frames.each { frame ->
				frame.init(renderer)
			}
		}

		@Override
		void render(GraphicsRenderer renderer) {

			def rotationFrame = rotationFrames()
			frames[rotationFrame].render(renderer)
			if (turretHeadings) {
				frames[headings + rotationFrame].render(renderer)
			}
		}

		protected int rotationFrames() {

			return unit.heading ? headings - (unit.heading / degreesPerHeading) : 0
		}
	}

	/**
	 * Renderer for drawing animations.
	 */
	private static class UnitAnimationRenderer extends UnitRenderer {

		private static final int FRAMERATE = 15 // C&C ran at 15fps?

		protected int framesPerHeading
		protected GameTime gameTime

		private long animationTimeStart

		protected UnitAnimationRenderer(String type, Unit unit, int headings, int framesPerHeading, Image[] frames,
			GameTime gameTime) {

			super(type, unit, headings, 0, frames)
			this.framesPerHeading = framesPerHeading
			this.gameTime = gameTime
		}

		@Override
		void render(GraphicsRenderer renderer) {

			def currentFrame = Math.floor((gameTime.currentTimeMillis - animationTimeStart) / 1000 * FRAMERATE) % framesPerHeading as int
			frames[rotationFrames() * framesPerHeading + currentFrame].render(renderer)
		}

		private void start() {

			animationTimeStart = gameTime.currentTimeMillis
		}
	}
}
