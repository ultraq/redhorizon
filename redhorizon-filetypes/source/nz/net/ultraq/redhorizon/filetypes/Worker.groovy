/* 
 * Copyright 2019, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.filetypes

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import java.nio.ByteBuffer

/**
 * A special {@link Runnable} with the ability to be controlled and queried from
 * other threads.  Worker implementations must check in with the various state
 * flags of this class to know when to continue/stop while performing their
 * work.  Workers then post data as it comes to all registered workers as a map
 * whose keys identify the kinds of data being returned.
 * 
 * @author Emanuel Rabina
 */
abstract class Worker implements Runnable {

	protected boolean canContinue
	protected boolean complete
	protected boolean running
	protected boolean stopped

	// TODO: This seems very much like events.  Maybe I should extract the event
	//       system in the engine package so it can be used here?
	protected List<Closure> dataHandlers = []

	/**
	 * Add a closure that will be called when data made by the worker is available
	 * for processing.
	 * 
	 * @param dataHandler
	 * @return
	 */
	Worker addDataHandler(
		@ClosureParams(value = SimpleType, options = ['java.lang.String', 'java.nio.ByteBuffer']) Closure dataHandler) {

		dataHandlers << dataHandler
		return this
	}

	/**
	 * Return whether the work is all done.
	 * 
	 * @return
	 */
	boolean isComplete() {

		return complete
	}

	/**
	 * Invoke all configured handlers with the given type and data so they can
	 * choose to process it or not.
	 * 
	 * @param type
	 * @param data
	 */
	protected void notifyHandlers(String type, ByteBuffer data) {

		dataHandlers.each { dataHandler ->
			dataHandler(type, data)
		}
	}

	@Override
	final void run() {

		running = true
		canContinue = true
		work()
		running = false
		complete = true
	}

	/**
	 * Signal to the worker to stop.
	 */
	void stop() {

		canContinue = false
		stopped = true
	}

	/**
	 * Start the worker.
	 */
	abstract void work()
}
