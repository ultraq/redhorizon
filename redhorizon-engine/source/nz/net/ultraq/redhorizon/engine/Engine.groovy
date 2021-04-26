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

import nz.net.ultraq.redhorizon.events.Event
import nz.net.ultraq.redhorizon.events.EventTarget

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Common code for any of the specific engine systems.
 * 
 * @author Emanuel Rabina
 */
abstract class Engine implements EventTarget, Runnable {

	private static final Logger logger = LoggerFactory.getLogger(Engine)

	protected final ExecutorService executorService = Executors.newSingleThreadExecutor()
	private final int targetRenderTimeMs

	protected boolean running

	/**
	 * Constructor, set the target render time.
	 * 
	 * @param targetRenderTimeMs
	 */
	protected Engine(int targetRenderTimeMs = 0) {

		this.targetRenderTimeMs = targetRenderTimeMs
	}

	/**
	 * Perform the render loop within a certain render budget, sleeping the thread
	 * if necessary to not exceed it.
	 * 
	 * @param closure
	 */
	protected void renderLoop(Closure closure) {

		running = true
		triggerOnSeparateThread(new RenderLoopStartEvent())

		try {
			while (shouldRender()) {
				def renderTime = time(closure)
				if (renderTime < targetRenderTimeMs) {
					Thread.sleep(targetRenderTimeMs - renderTime)
				}
			}
			triggerOnSeparateThread(new RenderLoopStopEvent())
		}
		catch (Exception ex) {
			logger.error('An error occurred during the render loop', ex)
			triggerOnSeparateThread(new RenderLoopStopEvent(ex))
		}
		finally {
			stop()
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

	/**
	 * Fire an event on a separate thread using the built-in executor.
	 * 
	 * @param event
	 */
	protected void triggerOnSeparateThread(Event event) {

		executorService.execute { ->
			trigger(event)
		}
	}
}
