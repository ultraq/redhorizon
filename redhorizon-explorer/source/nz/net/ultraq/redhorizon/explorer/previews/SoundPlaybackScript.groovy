/*
 * Copyright 2026, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.explorer.previews

import nz.net.ultraq.redhorizon.engine.audio.SoundComponent
import nz.net.ultraq.redhorizon.engine.scripts.Script

import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE

/**
 * Script for listening to a sound effect.
 *
 * @author Emanuel Rabina
 */
class SoundPlaybackScript extends Script implements AutoCloseable {

	private SoundComponent sound
	private boolean playbackStarted = false

	@Override
	void close() {

		sound.stop()
	}

	@Override
	void init() {

		sound = node.findComponentByType(SoundComponent)
	}

	@Override
	void update(float delta) {

		if (!playbackStarted) {
			sound.play()
			playbackStarted = true
		}

		if (input.keyPressed(GLFW_KEY_SPACE, true)) {
			if (sound.stopped) {
				sound.play()
			}
		}
	}
}
