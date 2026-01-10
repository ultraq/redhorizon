/*
 * Copyright 2025, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine.utilities

import groovy.transform.CompileStatic

/**
 * Calculate delta times for use in game loops.
 *
 * @author Emanuel Rabina
 */
@CompileStatic
class DeltaTimer {

	private long lastUpdateTimeNanos = System.nanoTime()

	/**
	 * The amount of time, in seconds, that has elapsed since the last call to
	 * this method.
	 */
	float deltaTime() {

		var currentTimeNanos = System.nanoTime()
		var delta = (currentTimeNanos - lastUpdateTimeNanos) / 1_000_000_000L
		lastUpdateTimeNanos = currentTimeNanos
		return delta as float
	}
}
