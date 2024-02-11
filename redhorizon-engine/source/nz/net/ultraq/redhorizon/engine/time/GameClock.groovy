/*
 * Copyright 2020, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine.time

import nz.net.ultraq.redhorizon.engine.EngineSystem
import nz.net.ultraq.redhorizon.engine.SystemReadyEvent
import nz.net.ultraq.redhorizon.engine.SystemStoppedEvent
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * A separate time source from the usual system time, allowing game time to flow
 * at different speeds.
 *
 * @author Emanuel Rabina
 */
class GameClock extends EngineSystem {

	private static Logger logger = LoggerFactory.getLogger(GameClock)

	private float speed = 1.0f
	private float lastSpeed

	GameClock(Scene scene) {

		super(scene)
	}

	/**
	 * Return whether or not time has been paused.
	 *
	 * @return
	 */
	boolean isPaused() {

		return speed == 0.0f
	}

	/**
	 * Pause the flow of time.
	 */
	void pause() {

		logger.debug('Pausing game clock')
		lastSpeed = speed
		speed = 0.0f
	}

	/**
	 * Resume the flow of time.
	 */
	void resume() {

		logger.debug('Resuming game clock')
		speed = lastSpeed
	}

	/**
	 * Starts the game clock and uses it to update temporal objects in the scene.
	 */
	@Override
	void run() {

		Thread.currentThread().name = 'Game clock'
		logger.debug('Starting game clock')

		scene.gameClock = this
		trigger(new SystemReadyEvent())

		var lastSystemTimeMillis = System.currentTimeMillis()
		long currentTimeMillis = lastSystemTimeMillis

		logger.debug('Game clock in update loop')
		while (!Thread.interrupted()) {
			try {
				rateLimit(100) { ->
					var currentSystemTimeMillis = System.currentTimeMillis()
					var delta = currentSystemTimeMillis - lastSystemTimeMillis

					// Normal flow of time, accumulate ticks at the same rate as system time
					if (speed == 1.0f) {
						currentTimeMillis += delta
					}
					// Modified flow, accumulate ticks at system time * flow speed
					else {
						currentTimeMillis += (delta * speed)
					}

					// Update time with scene objects
					scene.accept { element ->
						if (element instanceof Temporal) {
							element.tick(currentTimeMillis)
						}
					}

					lastSystemTimeMillis = currentSystemTimeMillis
				}
			}
			catch (InterruptedException ignored) {
				break
			}
		}

		trigger(new SystemStoppedEvent())
		logger.debug('Game clock stopped')
	}

	/**
	 * Toggles between paused and flowing states.
	 */
	void togglePause() {

		if (speed) {
			pause()
		}
		else {
			resume()
		}
	}
}
