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

package nz.net.ultraq.redhorizon

import nz.net.ultraq.redhorizon.engine.audio.AudioEngine
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsEngine
import nz.net.ultraq.redhorizon.filetypes.VideoFile
import nz.net.ultraq.redhorizon.media.Animation
import nz.net.ultraq.redhorizon.media.Video

import org.joml.Rectanglef
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.transform.TupleConstructor
import java.util.concurrent.CountDownLatch
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.FutureTask

/**
 * A basic video player, used primarily for testing purposes.
 * 
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class VideoPlayer {

	private static final Logger logger = LoggerFactory.getLogger(VideoPlayer)

	final VideoFile videoFile
	final boolean fixAspectRatio

	/**
	 * Play the video file.
	 */
	void play() {

		logger.info('File details: {}', videoFile)

		Executors.newCachedThreadPool().executeAndShutdown { executorService ->
			def width = videoFile.width * 2
			def height = (fixAspectRatio ? videoFile.height * 1.2 : videoFile.height) * 2
			def video = new Video(videoFile, new Rectanglef(-width / 2, -height / 2, width / 2, height / 2), executorService)

			def executionBarrier = new CyclicBarrier(2)
			def finishBarrier = new CountDownLatch(2)

			def audioEngine = new AudioEngine(video)

			// To allow the graphics engine to submit items to execute in this thread
			FutureTask executable = null
			def graphicsEngine = new GraphicsEngine(video, { toExecute ->
				executable = toExecute
				executionBarrier.await()
			})
			graphicsEngine.on(GraphicsEngine.EVENT_RENDER_LOOP_START) { event ->
				executorService.submit { ->
					Thread.currentThread().sleep(10000)
					logger.debug('Video started')
					video.play()
				}
			}

			// Stop both engines if one goes down
			audioEngine.on(AudioEngine.EVENT_RENDER_LOOP_STOP) { event ->
				graphicsEngine.stop()
				finishBarrier.countDown()
			}
			graphicsEngine.on(GraphicsEngine.EVENT_RENDER_LOOP_STOP) { event ->
				audioEngine.stop()
				finishBarrier.countDown()
			}

			video.on(Animation.EVENT_STOP) { event ->
				logger.debug('Video stopped')
				audioEngine.stop()
				graphicsEngine.stop()
			}

			def audioEngineTask = executorService.submit(audioEngine)
			def graphicsEngineTask = executorService.submit(graphicsEngine)

			logger.info('Waiting for video to finish.  Close the window to exit.')

			// Execute things from this thread when needed
			while (!graphicsEngineTask.done) {
				executionBarrier.await()
				if (executable) {
					executable.run()
					executable = null
					executionBarrier.reset()
				}

				// Shutdown phase
				if (graphicsEngine.started && graphicsEngine.stopped) {
					finishBarrier.await()
					break
				}
			}

			audioEngineTask.get()
			graphicsEngineTask.get()
		}
	}
}
