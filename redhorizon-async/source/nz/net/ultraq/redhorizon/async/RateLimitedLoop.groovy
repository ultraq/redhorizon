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

import groovy.transform.CompileStatic
import java.util.concurrent.FutureTask

/**
 * Similar to {@link ControlledLoop}, with the addition of the Thread being put
 * at the end of each loop execution to not exceed a specified frequency.
 * 
 * @author Emanuel Rabina
 */
@CompileStatic
class RateLimitedLoop implements RunnableWorker {

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

		def maxRunTimeMs = 1000 / frequency as long

		loopTask = new FutureTask<>({ ->
			def lastTimeMs = System.currentTimeMillis()
			while (!cancelled && loopCondition()) {
				loop()
				long diffTimeMs = System.currentTimeMillis() - lastTimeMs
				if (diffTimeMs < maxRunTimeMs) {
					long sleepTimeMs = maxRunTimeMs - diffTimeMs
					Thread.sleep(sleepTimeMs)
				}
				lastTimeMs = System.currentTimeMillis()
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
