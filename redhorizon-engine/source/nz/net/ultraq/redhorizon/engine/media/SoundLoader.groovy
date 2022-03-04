/* 
 * Copyright 2022, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine.media

import nz.net.ultraq.redhorizon.engine.GameClock
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.filetypes.SoundFile

import groovy.transform.TupleConstructor

/**
 * Load a sound effect or music track into existing engines.
 * 
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class SoundLoader implements MediaLoader<SoundFile, Playable> {

	final Scene scene
	final GameClock gameClock

	@Override
	Playable load(SoundFile soundFile) {

		def sound = soundFile.forStreaming ? new SoundTrack(soundFile, gameClock) : new SoundEffect(soundFile)
		scene << sound
		return sound
	}
}
