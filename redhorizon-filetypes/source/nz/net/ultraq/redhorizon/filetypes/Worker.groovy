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

import nz.net.ultraq.redhorizon.events.EventTarget

/**
 * A special {@link Runnable} with the ability to be controlled and queried from
 * other threads.  Worker implementations must check in with the various state
 * flags of this class to know when to continue/stop while performing their
 * work.  Workers then emit the data in events for any registered listeners to
 * act upon.
 * 
 * @author Emanuel Rabina
 */
abstract class Worker implements EventTarget, Runnable {

	protected boolean canContinue
	protected boolean complete
	protected boolean running
	protected boolean stopped

	/**
	 * Return whether the work is all done.
	 * 
	 * @return
	 */
	boolean isComplete() {

		return complete
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
