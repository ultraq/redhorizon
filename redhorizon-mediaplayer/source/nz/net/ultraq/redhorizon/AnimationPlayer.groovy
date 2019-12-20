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

import nz.net.ultraq.redhorizon.engine.graphics.GraphicsEngine
import nz.net.ultraq.redhorizon.filetypes.AnimationFile
import nz.net.ultraq.redhorizon.media.Animation

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.transform.TupleConstructor
import java.util.concurrent.Executors
import java.util.concurrent.FutureTask

/**
 * A basic animation player, used primarily for testing purposes.
 * 
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class AnimationPlayer {

	private static final Logger logger = LoggerFactory.getLogger(AnimationPlayer)

	final AnimationFile animationFile

	/**
	 * Play the configured animation file.
	 */
	void play() {

		logger.info("File details: ${animationFile}")

		Executors.newCachedThreadPool().executeAndShutdown { executorService ->
			def animation = new Animation(animationFile, executorService)

			// To allow the graphics engine to submit items to execute in this thread
			FutureTask executable = null
			def graphicsEngine = new GraphicsEngine(animation, { toExecute ->
				executable = toExecute
			})
			def engine = executorService.submit(graphicsEngine)
			graphicsEngine.on(GraphicsEngine.EVENT_RENDER_LOOP_START) { event ->
				executorService.submit { ->
					Thread.currentThread().sleep(500)
					logger.debug('Animation started')
					animation.play()
				}
			}

			animation.on(Animation.EVENT_STOP) { event ->
				logger.debug('Animation stopped')
				graphicsEngine.stop()
			}

			logger.info('Waiting for animation to finish.  Close the window to exit.')

			// Execute things from this thread when needed
			while (!engine.done) {
				while (!engine.done && !executable) {
					Thread.onSpinWait()
				}
				if (executable) {
					executable.run()
					executable = null
				}
			}

			engine.get()
		}
	}
}
