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

import nz.net.ultraq.redhorizon.events.EventTarget

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Common code for the engine subsystems.
 * 
 * @author Emanuel Rabina
 */
abstract class EngineSubsystem implements EventTarget, Runnable {

	private static final Logger logger = LoggerFactory.getLogger(EngineSubsystem)

	private final int targetRenderTimeMs

	protected boolean running

	/**
	 * Constructor, set the target render time.
	 * 
	 * @param targetRenderTimeMs
	 * @param renderLoopCondition
	 */
	protected EngineSubsystem(int targetRenderTimeMs = 0) {

		this.targetRenderTimeMs = targetRenderTimeMs
	}

	/**
	 * Perform the render loop within a certain render budget, sleeping the thread
	 * if necessary to not exceed it.
	 * 
	 * @param renderLoop
	 */
	protected void renderLoop(Closure renderLoop) {

		running = true
		trigger(new RenderLoopStartEvent())

		try {
			while (shouldRender()) {
				def loopStart = System.currentTimeMillis()
				renderLoop()
				def loopEnd = System.currentTimeMillis()

				def renderExecutionTime = loopEnd - loopStart
				if (renderExecutionTime < targetRenderTimeMs) {
					def waitTime = targetRenderTimeMs - renderExecutionTime
					Thread.sleep(waitTime)
				}
			}
		}
		catch (Exception ex) {
			logger.error('An error occurred during the render loop', ex)
		}
		finally {
			stop()
			trigger(new RenderLoopStopEvent())
		}
	}

	/**
	 * Return whether or not the render loop should be executed.  Used for
	 * checking if the engine is in a state to continue or if it should be
	 * shutting down for exiting.
	 * 
	 * @return
	 */
	protected boolean shouldRender() {

		return running
	}

	/**
	 * Stop this subsystem.  Signals to the subsystem to attempt a clean shutdown
	 * at the next available opportunity.
	 */
	void stop() {

		running = false
	}
}
