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

import nz.net.ultraq.eventhorizon.Event
import nz.net.ultraq.redhorizon.engine.audio.MusicComponent
import nz.net.ultraq.redhorizon.engine.scripts.EntityScript

import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE

/**
 * Script for listening to a music track.
 *
 * @author Emanuel Rabina
 */
class MusicPlaybackScript extends EntityScript implements AutoCloseable {

	private MusicComponent music
	private boolean playbackStarted = false

	@Override
	void close() {

		music.stop()
	}

	@Override
	void init() {

		music = entity.findComponentByType(MusicComponent)
		music.withVolume(0.5f)
	}

	@Override
	void update(float delta) {

		if (!playbackStarted) {
			music.play()
			playbackStarted = true
		}

		if (input.keyPressed(GLFW_KEY_SPACE, true)) {
			if (music.paused) {
				music.play()
			}
			else {
				music.pause()
			}
		}

		if (playbackStarted && music.stopped) {
			entity.trigger(new MusicStoppedEvent())
		}
	}

	/**
	 * Triggered when the music has finished playing by itself.
	 */
	static record MusicStoppedEvent() implements Event {}
}
