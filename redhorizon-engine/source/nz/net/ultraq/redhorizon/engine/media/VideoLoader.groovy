/*
 * Copyright 2022, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import nz.net.ultraq.redhorizon.engine.graphics.GraphicsSystem
import nz.net.ultraq.redhorizon.engine.input.InputEventStream
import nz.net.ultraq.redhorizon.engine.input.KeyControl
import nz.net.ultraq.redhorizon.engine.input.RemoveControlFunction
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.engine.time.GameClock
import nz.net.ultraq.redhorizon.filetypes.VideoFile

import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE

/**
 * Load a video file into existing engines.
 *
 * @author Emanuel Rabina
 */
class VideoLoader extends MediaLoader<VideoFile, Video> {

	private final GraphicsSystem graphicsEngine
	private final GameClock gameClock
	private final InputEventStream inputEventStream
	private RemoveControlFunction removePlayPauseControl

	/**
	 * Create a loader for an video file.
	 *
	 * @param videoFile
	 * @param scene
	 * @param graphicsEngine
	 * @param gameClock
	 * @param inputEventStream
	 */
	VideoLoader(VideoFile videoFile, Scene scene, GraphicsSystem graphicsEngine, GameClock gameClock,
		InputEventStream inputEventStream) {

		super(videoFile, scene)
		this.graphicsEngine = graphicsEngine
		this.gameClock = gameClock
		this.inputEventStream = inputEventStream
	}

	@Override
	Video load() {

		// Create a video and scale to fit the target size
		def width = file.width
		def height = file.height
		def scaleY = file.forVgaMonitors ? 1.2f : 1f
		def scale = graphicsEngine.window.renderResolution.calculateScaleToFit(width, height * scaleY as int)
		media = new Video(file)
			.scale(scale, scale * scaleY as float, 1)
			.translate(-width / 2, -height / 2)
		scene << media

		// Key events for controlling the video
		removePlayPauseControl = inputEventStream.addControl(new KeyControl(GLFW_KEY_SPACE, 'Play/Pause', { ->
			gameClock.togglePause()
		}))

		return media
	}

	@Override
	void unload() {

		if (media.playing) {
			media.stop()
		}
		file.streamingDecoder.stop()
		if (gameClock.paused) {
			gameClock.resume()
		}

		removePlayPauseControl.apply(null)
		scene.removeSceneElement(media)
	}
}
