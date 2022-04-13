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
 * Common code for any of the specific engine systems.
 * 
 * @author Emanuel Rabina
 */
abstract class Engine implements EventTarget, Runnable {

	private static final Logger logger = LoggerFactory.getLogger(Engine)

	/**
	 * Perform the main engine loop within a certain time budget, sleeping the
	 * thread if necessary to not overdo it.
	 * 
	 * @param closure
	 */
	protected void engineLoop(Closure closure) {

		trigger(new EngineLoopStartEvent())

		try {
			while (shouldRun()) {
				closure()
			}
			trigger(new EngineLoopStopEvent())
		}
		catch (Exception ex) {
			logger.error('An error occurred during the render loop', ex)
			trigger(new EngineLoopStopEvent(ex))
		}
		finally {
			stop()
		}
	}

	/**
	 * Return whether or not the engine loop should be executed.  Used for
	 * checking if the engine is in a state to continue or if it should be
	 * shutting down for exiting.
	 * 
	 * @return
	 */
	protected abstract boolean shouldRun()

	/**
	 * Stop this subsystem.  Signals to the subsystem to attempt a clean shutdown
	 * at the next available opportunity.
	 */
	abstract void stop()
}
