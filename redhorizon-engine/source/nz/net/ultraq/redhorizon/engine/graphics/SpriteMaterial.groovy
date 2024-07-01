/*
 * Copyright 2024, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine.graphics

/**
 * A material containing attributes for rendering sprites.
 *
 * @author Emanuel Rabina
 */
class SpriteMaterial extends Material {

	int frame
	float frameStepX
	float frameStepY
	int framesHorizontal
	int framesVertical

	/**
	 * Copy the values of an existing material into this one.
	 */
	SpriteMaterial copy(SpriteMaterial other) {

		super.copy(other)
		frame = other.frame
		frameStepX = other.frameStepX
		frameStepY = other.frameStepY
		framesHorizontal = other.framesHorizontal
		framesVertical = other.framesVertical
		return this
	}
}
