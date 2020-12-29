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

import nz.net.ultraq.redhorizon.engine.graphics.GraphicsElement
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.media.Image

/**
 * The base unit renderer for drawing simple static bodies.
 * 
 * @author Emanuel Rabina
 */
class UnitRenderer implements GraphicsElement {

	protected final String type
	protected final Unit unit
	protected final int headings
	protected final Image[] frames
	protected final float degreesPerHeading

	/**
	 * Constructor, create a unit renderer with the following frames.
	 * 
	 * @param type
	 * @param unit
	 * @param headings
	 * @param turretHeadings
	 * @param frames
	 */
	UnitRenderer(String type, Unit unit, int headings, Image[] frames) {

		this.type = type
		this.unit = unit
		this.headings = headings
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

		frames[rotationFrames()].render(renderer)
	}

	protected int rotationFrames() {

		return unit.heading ? headings - (unit.heading / degreesPerHeading) : 0
	}
}
