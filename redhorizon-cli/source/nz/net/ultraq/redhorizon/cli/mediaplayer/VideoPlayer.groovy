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

package nz.net.ultraq.redhorizon.cli.mediaplayer

import nz.net.ultraq.redhorizon.Application
import nz.net.ultraq.redhorizon.engine.EngineLoopStartEvent
import nz.net.ultraq.redhorizon.engine.audio.AudioConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.WindowCreatedEvent
import nz.net.ultraq.redhorizon.engine.input.KeyEvent
import nz.net.ultraq.redhorizon.filetypes.VideoFile
import nz.net.ultraq.redhorizon.media.StopEvent
import nz.net.ultraq.redhorizon.media.Video

import org.joml.Vector2f
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE
import static org.lwjgl.glfw.GLFW.GLFW_PRESS

/**
 * A basic video player, used primarily for testing purposes.
 * 
 * @author Emanuel Rabina
 */
class VideoPlayer extends Application {

	private static final Logger logger = LoggerFactory.getLogger(VideoPlayer)

	final VideoFile videoFile

	/**
	 * Constructor, set the video to be played.
	 * 
	 * @param audioConfig
	 * @param graphicsConfig
	 * @param videoFile
	 */
	VideoPlayer(AudioConfiguration audioConfig, GraphicsConfiguration graphicsConfig, VideoFile videoFile) {

		super(null, audioConfig, graphicsConfig)
		this.videoFile = videoFile
	}

	@Override
	void run() {

		logger.info('File details: {}', videoFile)

		// Add the video to the engines once we have the window dimensions
		Video video
		graphicsEngine.on(WindowCreatedEvent) { event ->
			def width = videoFile.width
			def height = videoFile.height
			def scale = calculateScaleForTarget(width, height, event.renderSize)
			def offset = new Vector2f(-width / 2, -height / 2)

			video = new Video(videoFile, gameClock)
			video.scaleXY(scale)
			video.translate(offset)

			video.on(StopEvent) { stopEvent ->
				stop()
				logger.debug('Video stopped')
			}
			scene << video
		}

		graphicsEngine.on(EngineLoopStartEvent) { event ->
			video.play()
			logger.debug('Video started')
		}

		logger.info('Waiting for video to finish.  Close the window to exit.')

		// Key event handler
		inputEventStream.on(KeyEvent) { event ->
			if (event.action == GLFW_PRESS) {
				switch (event.key) {
				case GLFW_KEY_SPACE:
					gameClock.togglePause()
					break
				case GLFW_KEY_ESCAPE:
					video.stop()
					break
				}
			}
		}
	}
}
