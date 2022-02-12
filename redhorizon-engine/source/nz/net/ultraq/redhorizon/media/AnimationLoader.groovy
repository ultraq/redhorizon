/* 
 * Copyright 2019, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.media

import nz.net.ultraq.redhorizon.engine.GameClock
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsEngine
import nz.net.ultraq.redhorizon.filetypes.AnimationFile
import nz.net.ultraq.redhorizon.scenegraph.Scene

import groovy.transform.TupleConstructor

/**
 * Load an animation file into existing engines.
 * 
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class AnimationLoader implements MediaLoader<AnimationFile, Animation> {

	final Scene scene
	final GraphicsEngine graphicsEngine
	final GameClock gameClock

	@Override
	Animation load(AnimationFile animationFile) {

		// Create an animation and scale it to fit the target size
		def width = animationFile.width
		def height = animationFile.height
		def scaleY = animationFile.forVgaMonitors ? 1.2f : 1f
		def scale = graphicsEngine.graphicsContext.renderResolution.calculateScaleToFit(width, height * scaleY as int)

		def animation = new Animation(animationFile, gameClock)
			.scale(scale, scale * scaleY as float, 1)
			.translate(-width / 2, -height / 2)
		scene << animation
		return animation
	}
}
