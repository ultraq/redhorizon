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

import java.util.concurrent.CountDownLatch

/**
 * Interface for engine subsystems.
 * 
 * @author Emanuel Rabina
 */
abstract class EngineSubsystem implements Runnable {

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

		while (running) {
			def loopStart = System.currentTimeMillis()
			renderLoop()
			def loopEnd = System.currentTimeMillis()

			def renderExecutionTime = loopEnd - loopStart
			if (renderExecutionTime < targetRenderTimeMs) {
				Thread.sleep(targetRenderTimeMs - renderExecutionTime)
			}
		}
	}

	/**
	 * Signal to stop this subsystem.
	 */
	void stop() {

		running = false
		stopLatch.await()
	}
}
