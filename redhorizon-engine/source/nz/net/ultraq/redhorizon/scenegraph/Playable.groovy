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

package nz.net.ultraq.redhorizon.scenegraph

/**
 * Interface defining methods for objects that can be heard by the player.
 * Primarily, they have control over whether to have these objects played at the
 * next rendering pass.
 * 
 * @author Emanuel Rabina
 */
interface Playable extends AudioElement {

	/**
	 * Queries the state of the playable, whether or not it's playing.
	 * 
	 * @return <tt>true</tt> if the sound is still playing, <tt>false</tt>
	 * 		   otherwise.
	 */
	boolean isPlaying()

	/**
	 * Requests that the object be played at the next audio rendering pass
	 * onwards.
	 */
	void play()

	/**
	 * Requests that the object stop any sounds that are currently playing.
	 */
	void stop()
}
