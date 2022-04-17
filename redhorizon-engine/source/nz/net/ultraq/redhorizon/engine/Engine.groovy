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


import nz.net.ultraq.redhorizon.async.RunnableWorker
import nz.net.ultraq.redhorizon.events.EventTarget

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Common code for any of the specific engine systems.
 * 
 * @author Emanuel Rabina
 */
abstract class Engine implements EventTarget, RunnableWorker {

	private static final Logger logger = LoggerFactory.getLogger(Engine)

	@Delegate
	private RunnableWorker engineLoop

	/**
	 * Perform the engine loop using the given worker implementation.
	 * 
	 * @param loopWorker
	 */
	protected void doEngineLoop(RunnableWorker loopWorker) {

		trigger(new EngineLoopStartEvent())

		try {
			engineLoop = loopWorker
			engineLoop.run()
			trigger(new EngineLoopStopEvent())
		}
		catch (Exception ex) {
			logger.error('An error occurred during the render loop', ex)
			trigger(new EngineLoopStopEvent(ex))
		}
	}
}
