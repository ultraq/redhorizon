/* 
 * Copyright 2007, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import nz.net.ultraq.redhorizon.events.EventTarget

/**
 * Trait for media that can be played forward.
 * 
 * @author Emanuel Rabina
 */
trait Playable extends EventTarget {

	private boolean playing = false

	/**
	 * Return whether or not the media is currently playing.
	 * 
	 * @return
	 */
	boolean isPlaying() {

		return playing
	}

	/**
	 * Put the object into the playing state.
	 */
	void play() {

		if (!playing) {
			playing = true
			trigger(new StartEvent())
		}
	}

	/**
	 * Take the object out of the playing state.
	 */
	void stop() {

		if (playing) {
			playing = false
			trigger(new StopEvent())
		}
	}
}
