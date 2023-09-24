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

import java.util.concurrent.FutureTask
import java.util.concurrent.RunnableFuture

/**
 * A special {@link RunnableFuture} that has a loop of repeating work that can
 * be queried and controlled from other objects.  Useful as a {@code @Delegate}
 * property in a class that needs to fulfil a {@code RunnableFuture<Void>}
 * class contract.
 *
 * @author Emanuel Rabina
 */
class ControlledLoop implements RunnableWorker {

	private static final Logger logger = LoggerFactory.getLogger(ControlledLoop)

	@Delegate
	private final FutureTask<Void> loopTask

	/**
	 * Constructor, build a {@link FutureTask} with a loop solely controlled by
	 * the task state.
	 *
	 * @param loop
	 */
	ControlledLoop(Closure loop) {

		this({ true }, loop)
	}

	/**
	 * Constructor, build a {@link FutureTask} with a loop based around the given
	 * parameters.
	 *
	 * @param loopCondition
	 * @param loop
	 */
	ControlledLoop(Closure loopCondition, Closure loop) {

		loopTask = new FutureTask<>({ ->
			try {
				while (!cancelled && loopCondition()) {
					loop()
				}
			}
			catch (Throwable ex) {
				logger.error("An error occurred in controlled loop \"${Thread.currentThread().name}\".  Exiting...", ex)
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
