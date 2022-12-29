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

package nz.net.ultraq.redhorizon.engine.media

import nz.net.ultraq.redhorizon.engine.GameClock
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsEngine
import nz.net.ultraq.redhorizon.engine.input.InputEventStream
import nz.net.ultraq.redhorizon.engine.input.KeyControl
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.filetypes.AnimationFile
import nz.net.ultraq.redhorizon.filetypes.Streaming

import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE

/**
 * Load an animation file into existing engines.
 * 
 * @author Emanuel Rabina
 */
class AnimationLoader extends MediaLoader<AnimationFile, Animation> {

	private final GraphicsEngine graphicsEngine
	private final GameClock gameClock
	private final InputEventStream inputEventStream
	private final KeyControl playPauseControl

	/**
	 * Create a loader for an animation file.
	 * 
	 * @param animationFile
	 * @param scene
	 * @param graphicsEngine
	 * @param gameClock
	 * @param inputEventStream
	 */
	AnimationLoader(AnimationFile animationFile, Scene scene, GraphicsEngine graphicsEngine, GameClock gameClock,
		InputEventStream inputEventStream) {

		super(animationFile, scene)
		this.graphicsEngine = graphicsEngine
		this.gameClock = gameClock
		this.inputEventStream = inputEventStream

		playPauseControl = new KeyControl(GLFW_KEY_SPACE, 'Play/Pause') {
			@Override
			void handleKeyPress() {
				gameClock.togglePause()
			}
		}
	}

	@Override
	Animation load() {

		// Create an animation and scale it to fit the target size
		def width = file.width
		def height = file.height
		def scaleY = file.forVgaMonitors ? 1.2f : 1f
		def scale = graphicsEngine.graphicsContext.renderResolution.calculateScaleToFit(width, height * scaleY as int)
		media = new Animation(file, gameClock)
			.scale(scale, scale * scaleY as float, 1)
			.translate(-width / 2, -height / 2)
		scene << media

		// Key events for controlling the animation
		inputEventStream.addControl(playPauseControl)

		return media
	}

	@Override
	void unload() {

		if (media.playing) {
			media.stop()
			if (file instanceof Streaming) {
				file.streamingDataWorker.stop()
			}
		}
		inputEventStream.removeControl(playPauseControl)
		scene.removeSceneElement(media)
	}
}
