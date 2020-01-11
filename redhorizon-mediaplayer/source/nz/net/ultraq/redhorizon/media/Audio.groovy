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

package nz.net.ultraq.redhorizon.media

import nz.net.ultraq.redhorizon.engine.audio.AudioEngine

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import java.util.concurrent.ExecutorService

/**
 * Common behaviour for media players that need an audio engine for playback.
 * 
 * @author Emanuel Rabina
 */
trait Audio {

	/**
	 * Execute the given closure within the context of having an audio engine:
	 * setting it up, passing it along to the closure, and finally shutting it
	 * down.
	 * 
	 * @param executorService
	 * @param closure
	 */
	void withAudioEngine(ExecutorService executorService,
		@ClosureParams(value = SimpleType, options = 'nz.net.ultraq.redhorizon.engine.audio.AudioEngine')
		Closure closure) {

		def audioEngine = new AudioEngine()
		def engine = executorService.submit(audioEngine)

		closure(audioEngine)

		engine.get()
	}
}
