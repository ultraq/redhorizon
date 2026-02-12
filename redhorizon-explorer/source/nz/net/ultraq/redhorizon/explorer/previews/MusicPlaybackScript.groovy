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
import nz.net.ultraq.redhorizon.audio.Music
import nz.net.ultraq.redhorizon.engine.scripts.Script

import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE

/**
 * Script for listening to a music track.
 *
 * @author Emanuel Rabina
 */
class MusicPlaybackScript extends Script<Music> implements AutoCloseable {

	private boolean playbackStarted = false

	@Override
	void close() {

		node.stop()
	}

	@Override
	void init() {

		node.withVolume(0.5f)
	}

	@Override
	void update(float delta) {

		node.update()

		if (!playbackStarted) {
			node.play()
			playbackStarted = true
		}

		if (input.keyPressed(GLFW_KEY_SPACE, true)) {
			if (node.paused) {
				node.play()
			}
			else {
				node.pause()
			}
		}

		if (playbackStarted && node.stopped) {
			node.trigger(new MusicStoppedEvent())
		}
	}

	/**
	 * Triggered when the music has finished playing by itself.
	 */
	static record MusicStoppedEvent() implements Event {}
}
