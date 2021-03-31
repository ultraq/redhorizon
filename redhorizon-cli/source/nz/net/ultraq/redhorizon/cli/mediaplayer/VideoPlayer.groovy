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
import nz.net.ultraq.redhorizon.engine.RenderLoopStartEvent
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.WindowCreatedEvent
import nz.net.ultraq.redhorizon.engine.input.KeyEvent
import nz.net.ultraq.redhorizon.filetypes.VideoFile
import nz.net.ultraq.redhorizon.geometry.Dimension
import nz.net.ultraq.redhorizon.media.StopEvent
import nz.net.ultraq.redhorizon.media.Video
import nz.net.ultraq.redhorizon.scenegraph.Scene

import org.joml.Vector2f
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
class VideoPlayer extends Application {

	private static final Logger logger = LoggerFactory.getLogger(VideoPlayer)

	final VideoFile videoFile
	final GraphicsConfiguration graphicsConfig
	final boolean scaleLowRes
	final boolean scanlines

	/**
	 * Play the video file.
	 */
	void play() {

		logger.info('File details: {}', videoFile)

		def scene = new Scene()

		Executors.newCachedThreadPool().executeAndShutdown { executorService ->
			useGameClock(executorService) { gameClock ->
				useAudioEngine(scene, executorService) { audioEngine ->
					useGraphicsEngine(scene, executorService, graphicsConfig) { graphicsEngine ->

						// Add the video to the engines once we have the window dimensions
						Video video
						graphicsEngine.on(WindowCreatedEvent) { event ->
							def width = videoFile.width
							def height = videoFile.height
							if (scaleLowRes) {
								width <<= 1
								height <<= 1
							}
							def scale = calculateScaleForFullScreen(width, height, event.cameraSize)
							def offset = new Vector2f(-width / 2, -height / 2)

							video = new Video(videoFile, scaleLowRes, gameClock, executorService)
							video.scaleXY(scale)
							video.translate(offset)

							video.on(StopEvent) { stopEvent ->
								logger.debug('Video stopped')
								audioEngine.stop()
								graphicsEngine.stop()
							}
							scene << video

							if (scanlines) {
								scene << new Scanlines(new Dimension(width, height))
									.scaleXY(scale)
									.translate(offset)
									.translate(0, -scale / 2 as float, 0)
							}
						}

						graphicsEngine.on(RenderLoopStartEvent) { event ->
							video.play()
							logger.debug('Video started')
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
