/* 
 * Copyright 2023, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import nz.net.ultraq.redhorizon.engine.graphics.GraphicsElement
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.Mesh

import org.joml.primitives.Rectanglef
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.transform.TupleConstructor

/**
 * Class for a particular state that a unit can be in, which is usually conveyed
 * with its own sprite or animation.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class UnitState implements GraphicsElement {

	private static final Logger logger = LoggerFactory.getLogger(UnitState)
	private static final int FRAMERATE = 10 // C&C ran animations at 10fps?

	final Unit unit
	final String name
	final int headings
	final int frames
	final int frameOffset

	// 2D array of meshes.  The first index is for the particular heading, the
	// second is the frame of animation within that heading
	private Mesh[][] meshes

	@Override
	void delete(GraphicsRenderer renderer) {

		meshes.each { meshesPerHeading ->
			meshesPerHeading.each { mesh ->
				renderer.deleteMesh(mesh)
			}
		}
	}

	@Override
	void init(GraphicsRenderer renderer) {

		meshes = new Mesh[headings][frames]

		headings.times { heading ->
			frames.times { frame ->
				var frameIndex = frameOffset + (heading ? (headings - heading) * frames : 0) + frame
				var textureU = (frameIndex % unit.spritesHorizontal) * unit.frameStepX as float
				var textureV = Math.floor(frameIndex / unit.spritesHorizontal) * unit.frameStepY as float
				var textureUvs = new Rectanglef(
					textureU,
					textureV,
					textureU + unit.frameStepX as float,
					textureV + unit.frameStepY as float
				)
				meshes[heading][frame] = renderer.createSpriteMesh(
					surface: new Rectanglef(0, 0, unit.width, unit.height),
					textureUVs: textureUvs
				)
			}
		}
	}

	@Override
	void render(GraphicsRenderer renderer) {

		// Pick heading and frame in heading
		var currentHeading = headings > 1 ? Math.round(unit.heading / (360f / headings)) as int : 0
		var currentFrame = frames > 1 && unit.animationStartTime ?
			Math.floor((unit.currentTimeMs - unit.animationStartTime) / 1000 * FRAMERATE) % frames as int :
			0

		try {
			renderer.draw(meshes[currentHeading][currentFrame], unit.shader, unit.material)
		}
		catch (ArrayIndexOutOfBoundsException ex) {
			logger.error('Invalid frame for the current state.  State name: {}, heading: {}, frame: {}',
				name, currentHeading, currentFrame)
			throw ex
		}
	}
}
