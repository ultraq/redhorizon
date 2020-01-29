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

import nz.net.ultraq.redhorizon.engine.graphics.WindowCreatedEvent
import nz.net.ultraq.redhorizon.filetypes.AnimationFile
import nz.net.ultraq.redhorizon.geometry.Dimension
import nz.net.ultraq.redhorizon.media.Animation
import nz.net.ultraq.redhorizon.media.StopEvent

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.transform.TupleConstructor
import java.util.concurrent.Executors

/**
 * A basic animation player, used primarily for testing purposes.
 * 
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class AnimationPlayer implements Visual {

	private static final Logger logger = LoggerFactory.getLogger(AnimationPlayer)

	final AnimationFile animationFile

	final boolean filtering
	final boolean fixAspectRatio
	final boolean scaleLowRes
	final boolean scanlines

	/**
	 * Play the configured animation file.
	 */
	void play() {

		logger.info('File details: {}', animationFile)

		Executors.newCachedThreadPool().executeAndShutdown { executorService ->
			withGraphicsEngine(executorService, fixAspectRatio) { graphicsEngine ->

				// Add the animation to the engine once we have the window dimensions
				def animation
				graphicsEngine.on(WindowCreatedEvent) { event ->
					def animationCoordinates = calculateCenteredDimensions(animationFile.width, animationFile.height,
						fixAspectRatio, event.windowSize)

					animation = new Animation(animationFile, animationCoordinates, filtering, scaleLowRes, executorService)
					animation.on(StopEvent) { stopEvent ->
						logger.debug('Animation stopped')
						graphicsEngine.stop()
					}
					graphicsEngine.addSceneElement(animation)

					if (scanlines) {
						graphicsEngine.addSceneElement(new Scanlines(
							new Dimension(animationFile.width, animationFile.height),
							animationCoordinates
						))
					}

					animation.play()
					logger.debug('Animation started')

					logger.info('Waiting for animation to finish.  Close the window to exit.')
				}
			}
		}
	}
}
