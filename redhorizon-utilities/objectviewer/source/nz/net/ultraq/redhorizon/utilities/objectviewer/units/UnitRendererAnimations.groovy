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

import nz.net.ultraq.redhorizon.engine.GameTime
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.Material
import nz.net.ultraq.redhorizon.engine.graphics.Mesh

import org.joml.Rectanglef

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

	private Material material
	private Mesh mesh
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
	 * @param gameTime
	 */
	UnitRendererAnimations(String type, Unit unit, int headings, int framesPerHeading, ByteBuffer[] imageData,
		GameTime gameTime) {

		super(type, unit, headings, imageData)
		this.framesPerHeading = framesPerHeading
		this.gameTime = gameTime
	}

	@Override
	void delete(GraphicsRenderer renderer) {

		super.delete(renderer)
		renderer.deleteMesh(mesh)
	}

	@Override
	void init(GraphicsRenderer renderer) {

		super.init(renderer)
		mesh = renderer.createSpriteMesh(new Rectanglef(0, 0, unit.width, unit.height))
		material = renderer.createMaterial(mesh, null)
			.scale(unit.scale)
			.translate(unit.position)
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
