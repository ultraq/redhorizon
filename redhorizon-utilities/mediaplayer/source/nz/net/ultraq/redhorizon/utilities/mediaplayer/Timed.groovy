/* 
 * Copyright 2020, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.utilities.mediaplayer

import nz.net.ultraq.redhorizon.engine.GameClock

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import java.util.concurrent.ExecutorService

/**
 * Common behaviour for all media players that utilize game time.
 * 
 * @author Emanuel Rabina
 */
trait Timed {

	/**
	 * Execute the given closure within the context of having a game clock:
	 * setting it up, passing it to the closure, and shutting it down when the
	 * closure is complete.
	 * 
	 * @param executorService
	 * @param closure
	 */
	void withGameClock(ExecutorService executorService,
		@ClosureParams(value = SimpleType, options = 'nz.net.ultraq.redhorizon.engine.GameClock')
		Closure closure) {

		def gameClock = new GameClock(executorService)
		closure(gameClock)
		gameClock.stop()
	}
}
