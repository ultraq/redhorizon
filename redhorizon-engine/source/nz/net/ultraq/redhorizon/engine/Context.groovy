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

package nz.net.ultraq.redhorizon.engine

import nz.net.ultraq.redhorizon.events.EventTarget

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.ExecutorService

/**
 * Common methods for OpenAL/GL execution contexts.
 * 
 * @author Emanuel Rabina
 */
abstract class Context implements Closeable, EventTarget {

	private static final Logger logger = LoggerFactory.getLogger(Context)

	protected final ExecutorService executorService

	/**
	 * Constructor, set the executor for firing async events.
	 * 
	 * @param executorService
	 */
	protected Context(ExecutorService executorService) {

		this.executorService = executorService
	}

	/**
	 * Makes the context current on the executing thread.
	 */
	abstract void makeCurrent()

	/**
	 * Releases the context that is current on the executing thread.
	 */
	abstract void releaseCurrent()

	/**
	 * Execute the given closure with the context current on the executing thread,
	 * releasing it at the end.
	 * 
	 * @param closure
	 */
	void withCurrent(Closure closure) {

		try {
			makeCurrent()
			closure()
		}
		catch (Throwable ex) {
			logger.error('An error occurred within the scope of the context', ex)
			trigger(new ContextErrorEvent(ex))
		}
		finally {
			releaseCurrent()
		}
	}
}
