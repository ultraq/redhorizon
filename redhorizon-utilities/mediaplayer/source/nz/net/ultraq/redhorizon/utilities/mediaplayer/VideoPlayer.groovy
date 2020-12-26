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

package nz.net.ultraq.redhorizon.utilities.mediaplayer

import nz.net.ultraq.redhorizon.engine.KeyEvent
import nz.net.ultraq.redhorizon.engine.RenderLoopStartEvent
import nz.net.ultraq.redhorizon.engine.WithGameClock
import nz.net.ultraq.redhorizon.engine.audio.WithAudioEngine
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.WindowCreatedEvent
import nz.net.ultraq.redhorizon.engine.graphics.WithGraphicsEngine
import nz.net.ultraq.redhorizon.filetypes.VideoFile
import nz.net.ultraq.redhorizon.geometry.Dimension
import nz.net.ultraq.redhorizon.media.StopEvent
import nz.net.ultraq.redhorizon.media.Video

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE
import static org.lwjgl.glfw.GLFW.GLFW_PRESS

import groovy.transform.TupleConstructor
import java.util.concurrent.Executors

/**
 * A basic video player, used primarily for testing purposes.
 * 
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class VideoPlayer implements WithAudioEngine, WithGameClock, WithGraphicsEngine {

	private static final Logger logger = LoggerFactory.getLogger(VideoPlayer)

	final VideoFile videoFile

	final boolean filter
	final boolean fixAspectRatio
	final boolean scaleLowRes
	final boolean scanlines

	/**
	 * Play the video file.
	 */
	void play() {

		logger.info('File details: {}', videoFile)

		def config = new GraphicsConfiguration(
			filter: filter,
			fixAspectRatio: fixAspectRatio
		)

		Executors.newCachedThreadPool().executeAndShutdown { executorService ->
			withGameClock(executorService) { gameClock ->
				withAudioEngine(executorService) { audioEngine ->
					withGraphicsEngine(executorService, config) { graphicsEngine ->

						// Add the video to the engines once we have the window dimensions
						Video video
						graphicsEngine.on(WindowCreatedEvent) { event ->
							def videoCoordinates = calculateCenteredDimensions(videoFile.width, videoFile.height, event.windowSize)

							video = new Video(videoFile, videoCoordinates, scaleLowRes, gameClock, executorService)
							video.on(StopEvent) { stopEvent ->
								logger.debug('Video stopped')
								audioEngine.stop()
								graphicsEngine.stop()
							}
							audioEngine.addSceneElement(video)
							graphicsEngine.addSceneElement(video)

							if (scanlines) {
								graphicsEngine.addSceneElement(new Scanlines(
									new Dimension(videoFile.width, videoFile.height),
									videoCoordinates
								))
							}
						}

						graphicsEngine.on(RenderLoopStartEvent) { event ->
							executorService.submit { ->
								Thread.currentThread().name = 'Video starter'
								video.play()
								logger.debug('Video started')
							}
						}

						logger.info('Waiting for video to finish.  Close the window to exit.')

						// Key event handler
						graphicsEngine.on(KeyEvent) { event ->
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
			}
		}
	}
}
