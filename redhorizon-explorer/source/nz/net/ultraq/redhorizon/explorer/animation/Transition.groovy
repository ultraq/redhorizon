/*
 * Copyright 2024, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.explorer.animation

import groovy.transform.TupleConstructor
import java.util.concurrent.CompletableFuture

/**
 * A transition is a combination of an easing function, a duration, and a
 * callback to be notified of progress along the easing function, all combined
 * to animate objects.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class Transition {

	/**
	 * An easing function is one that takes a value between 0 and 1 that
	 * represents progress over time, and transforms it into another value
	 * between 0 and 1 that represents progress in space.
	 */
	final EasingFunction easingFunction

	final int durationMs

	/**
	 * Called with the result of the easing function to adjust some property of an
	 * object to animate it.
	 */
	final Closure callback

	/**
	 * Starts the transition.
	 *
	 * @return A {@code Future} that will be completed when the transition is.
	 */
	CompletableFuture<Void> start() {

		var startTimeMs = System.currentTimeMillis()
		var endTimeMs = startTimeMs + durationMs
		return CompletableFuture.runAsync { ->
			while (!Thread.interrupted()) {
				var currentTimeMs = System.currentTimeMillis()
				if (currentTimeMs > endTimeMs) {
					callback(easingFunction.transform(1))
					break
				}
				callback(easingFunction.transform((currentTimeMs - startTimeMs) / durationMs))
				Thread.sleep(10)
			}
		}
	}
}
