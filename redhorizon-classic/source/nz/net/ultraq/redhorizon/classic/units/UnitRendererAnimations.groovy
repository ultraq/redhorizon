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

import nz.net.ultraq.redhorizon.engine.GameTime
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.filetypes.Palette

import java.nio.ByteBuffer

/**
 * Renderer for drawing animations.
 * 
 * @author Emanuel Rabina
 */
class UnitRendererAnimations extends UnitRenderer {

	private static final int FRAMERATE = 10 // C&C ran animations at 10fps?

	protected int framesPerHeading
	protected GameTime gameTime

	private long animationTimeStart

	/**
	 * Constructor, create a unit renderer for animations with the following
	 * frames.
	 * 
	 * @param type
	 * @param unit
	 * @param headings
	 * @param framesPerHeading
	 * @param imageData
	 * @param palette
	 * @param gameTime
	 */
	UnitRendererAnimations(String type, Unit unit, int headings, int framesPerHeading, ByteBuffer[] imageData,
		Palette palette, GameTime gameTime) {

		super(type, unit, headings, imageData, palette)
		this.framesPerHeading = framesPerHeading
		this.gameTime = gameTime
	}

	@Override
	void render(GraphicsRenderer renderer) {

		def currentFrame = Math.floor((gameTime.currentTimeMillis - animationTimeStart) / 1000 * FRAMERATE) % framesPerHeading as int
		material.texture = textures[rotationFrames() * framesPerHeading + currentFrame]
		renderer.drawMaterial(material)
	}

	/**
	 * Start playing the animation.
	 */
	void start() {

		animationTimeStart = gameTime.currentTimeMillis
	}
}
