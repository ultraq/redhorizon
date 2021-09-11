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

package nz.net.ultraq.redhorizon.engine

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * An implementation of the separate time source of game time that can be
 * controlled to affect it.
 * 
 * @author Emanuel Rabina
 */
class GameClock implements GameTime, Runnable {

	private static Logger logger = LoggerFactory.getLogger(GameClock)

	private float speed = 1.0f
	private float lastSpeed
	private boolean running = true
	long currentTimeMillis

	@Override
	boolean isPaused() {

		return !speed
	}

	/**
	 * Pauses the flow of time.
	 */
	void pause() {

		logger.debug('Pausing game clock')
		lastSpeed = speed
		speed = 0.0f
	}

	/**
	 * Resumes the flow of time.
	 */
	void resume() {

		logger.debug('Resuming game clock')
		speed = lastSpeed
	}

	@Override
	void run() {

		def lastSystemTimeMillis = System.currentTimeMillis()
		currentTimeMillis = lastSystemTimeMillis

		Thread.currentThread().name = 'Game clock'
		while (running) {
			sleep(1) { ex ->
				running = false
				return true
			}
			def currentSystemTimeMillis = System.currentTimeMillis()
			def diff = currentSystemTimeMillis - lastSystemTimeMillis

			// Normal flow of time, accumulate ticks at the same rate as system time
			if (speed == 1.0f) {
				currentTimeMillis += diff
			}
			// Modified flow, accumulate ticks at system time * flow speed
			else {
				currentTimeMillis += (diff * speed)
			}
			lastSystemTimeMillis = currentSystemTimeMillis
		}
	}

	/**
	 * Stop the ticking of the game clock.
	 */
	void stop() {

		running = false
	}

	/**
	 * Toggles between a paused and flowing state.
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
