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

import java.util.concurrent.ExecutorService

/**
 * An interface for a special type of class that returns streaming data.  The
 * single method, {@link #work} uses the given closure to determine what to do
 * with the data chunks, allowing callers to either let the data build up or to
 * block until the data can be read.
 * 
 * @author Emanuel Rabina
 */
interface Worker {

	/**
	 * Return whether the work is all done.
	 * 
	 * @return
	 */
	boolean isComplete()

	/**
	 * Signal to the worker to stop.
	 */
	void stop()

	/**
	 * Start the worker, passing resulting chunks to the given closure.
	 * 
	 * @param executorService
	 * @param handler
	 */
	void work(ExecutorService executorService, Closure handler)
}
