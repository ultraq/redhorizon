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

package nz.net.ultraq.redhorizon.media

import nz.net.ultraq.redhorizon.engine.GameClock
import nz.net.ultraq.redhorizon.engine.input.InputEventStream
import nz.net.ultraq.redhorizon.engine.input.KeyEvent
import nz.net.ultraq.redhorizon.events.EventTarget
import nz.net.ultraq.redhorizon.filetypes.SoundFile
import nz.net.ultraq.redhorizon.scenegraph.Scene

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE
import static org.lwjgl.glfw.GLFW.GLFW_PRESS

import groovy.transform.TupleConstructor

/**
 * Load a sound effect or music track into existing engines.
 * 
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class SoundLoader implements MediaLoader<SoundFile, Playable>, EventTarget {

	final Scene scene
	final InputEventStream inputEventStream
	final GameClock gameClock

	@Override
	Playable load(SoundFile soundFile) {

		// Try determine the appropriate media for the sound file
		def sound = soundFile.forStreaming ? new SoundTrack(soundFile, gameClock) : new SoundEffect(soundFile)

		// Key event handler
		inputEventStream.on(KeyEvent) { event ->
			if (event.action == GLFW_PRESS) {
				switch (event.key) {
					case GLFW_KEY_ESCAPE:
						sound.stop()
						break
				}
			}
		}

		scene << sound

		sound.relay(StartEvent, this)
		sound.relay(StopEvent, this)

		return sound
	}
}
