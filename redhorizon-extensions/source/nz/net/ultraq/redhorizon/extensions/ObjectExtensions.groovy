/* 
 * Copyright 2019, The Thymeleaf Project (http://www.thymeleaf.org/)
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

package nz.net.ultraq.redhorizon.extensions

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Extensions to the main Object!
 * 
 * @author Emanuel Rabina
 */
class ObjectExtensions {

	private static final Logger logger = LoggerFactory.getLogger(ObjectExtensions)

	/**
	 * Capture and log the time it takes to perform the given closure.
	 * 
	 * @param <T>
	 * @param actionName
	 * @param closure
	 * @return
	 */
	static <T> T time(Object self, String actionName, Closure<T> closure) {

		def start = System.currentTimeMillis()
		def result = closure()
		def finish = System.currentTimeMillis()
		logger.debug("${actionName} complete.  Execution time: ${finish - start}ms")
		return result
	}
}
