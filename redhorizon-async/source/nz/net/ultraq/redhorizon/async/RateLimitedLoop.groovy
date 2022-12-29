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

package nz.net.ultraq.redhorizon.async

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.transform.CompileStatic
import java.util.concurrent.FutureTask

/**
 * Similar to {@link ControlledLoop}, with the addition of the Thread being put
 * into wait states  at the end of each loop execution to not exceed a specified
 * frequency.
 * 
 * @author Emanuel Rabina
 */
@CompileStatic
class RateLimitedLoop implements RunnableWorker {

	private final Logger logger = LoggerFactory.getLogger(RateLimitedLoop)

	@Delegate
	final FutureTask<Void> loopTask

	/**
	 * Constructor, build a {@link FutureTask} with a rate-limited loop solely
	 * controlled by the task state.

	 * @param frequency
	 * @param loop
	 */
	RateLimitedLoop(float frequency, Closure loop) {

		this(frequency, { true }, loop)
	}

	/**
	 * Constructor, build a {@link FutureTask} with a rate-limited loop based
	 * around the given parameters.
	 * 
	 * @param frequency
	 * @param loopCondition
	 * @param loop
	 */
	RateLimitedLoop(float frequency, Closure loopCondition, Closure loop) {

		double maxRunTimeNanos = 1000000000 / frequency

		loopTask = new FutureTask<>({ ->
			try {
				def lastTimeNanos = System.nanoTime()
				while (!cancelled && loopCondition()) {
					loop()

					// Add a wait if loop() was too quick
					long executionTimeNanos = System.nanoTime() - lastTimeNanos
					if (executionTimeNanos < maxRunTimeNanos) {
						double diffTimeNanos = maxRunTimeNanos - executionTimeNanos
						// If the amount to wait is rather large, sleep for most of that time
						// (all but the remaining 5ms of the time because sleep is a minimum,
						// it can take a bit to calculate all of this, and to 'wake up')
						if (diffTimeNanos > 5000000) {
							Thread.sleep((long) (diffTimeNanos / 1000000 - 5))
						}
						// Go into a spin-lock for the remaining time
						while (System.nanoTime() - lastTimeNanos < maxRunTimeNanos) {
							Thread.onSpinWait()
						}
					}

					lastTimeNanos = System.nanoTime()
				}
			}
			catch (Exception ex) {
				logger.error("An error occurred in a rate-limited loop \"${Thread.currentThread().name}\"", ex)
			}
		}, null)
	}

	@Override
	boolean isStopped() {

		return isDone()
	}

	@Override
	void stop() {

		cancel()
	}
}
