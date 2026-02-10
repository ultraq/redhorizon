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
import nz.net.ultraq.redhorizon.engine.scripts.Script
import nz.net.ultraq.redhorizon.graphics.Video

/**
 * Control the behaviour of a video in preview.
 *
 * @author Emanuel Rabina
 */
class VideoPlaybackScript extends Script<Video> implements AutoCloseable {

	private boolean playbackStarted = false

	@Override
	void close() {

		node.stop()
	}

	@Override
	void update(float delta) {

		if (!playbackStarted) {
			node.play()
			playbackStarted = true
		}

		if (playbackStarted && node.stopped) {
			node.trigger(new VideoStoppedEvent())
		}
	}

	/**
	 * Triggered when a video has stopped playing by itself.
	 */
	static record VideoStoppedEvent() implements Event {}
}
