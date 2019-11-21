/* 
 * Copyright 2016, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Interface for engine subsystems.
 * 
 * @author Emanuel Rabina
 */
abstract class EngineSubsystem implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(EngineSubsystem)

	protected final int targetRenderTimeMs
	protected final CountDownLatch stopLatch = new CountDownLatch(1)

	protected boolean running

	/**
	 * Constructor, set the target render time.
	 * 
	 * @param targetRenderTimeMs
	 */
	protected EngineSubsystem(int targetRenderTimeMs) {

		this.targetRenderTimeMs = targetRenderTimeMs
	}

	/**
	 * Perform the render loop within a certain render budget, sleeping the thread
	 * if necessary to not exceed it.
	 *
	 * @param renderLoop
	 */
	protected void renderLoop(Closure renderLoop) {

		try {
			running = true
			while (running) {
				def loopStart = System.currentTimeMillis()
				logger.debug('Render loop')
				renderLoop()
				def loopEnd = System.currentTimeMillis()

				def renderExecutionTime = loopEnd - loopStart
				if (renderExecutionTime < targetRenderTimeMs) {
					def waitTime = targetRenderTimeMs - renderExecutionTime
					logger.debug("Sleeping for ${waitTime}ms")
					Thread.sleep(waitTime)
				}
				else {
					logger.debug('Not sleeping')
				}
			}
		}
		finally {
			stopLatch.countDown()
		}
	}

	/**
	 * Signal to stop this subsystem.
	 */
	void stop() {

		logger.debug('Stop called')
		running = false
		stopLatch.await(5, TimeUnit.SECONDS)
	}
}
