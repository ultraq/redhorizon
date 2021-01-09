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

package nz.net.ultraq.redhorizon.utilities.objectviewer.units

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
 * The unit that gets displayed on the screen.
 * 
 * @author Emanuel Rabina
 */
abstract class Unit implements GraphicsElement, SelfVisitable {

	private static final Logger logger = LoggerFactory.getLogger(Unit)

	protected final List<UnitRenderer> unitRenderers = []
	protected UnitRenderer currentRenderer
	protected float heading

	/**
	 * Build a series of images from the specified range of frame data of the unit
	 * graphics.
	 * 
	 * @param imagesFile
	 * @param palette
	 * @param coordinates
	 * @param range
	 * @return
	 */
	protected static Image[] buildImages(ImagesFile imagesFile, Palette palette, Rectanglef coordinates, IntRange range) {

		return range.collect { i ->
			return new Image(imagesFile, i, coordinates, palette)
		} as Image[]
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
		if (currentRenderer instanceof UnitRendererAnimations) {
			currentRenderer.start()
		}
		logger.debug("${currentRenderer.type} animation selected")
	}
}
