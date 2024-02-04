/*
 * Copyright 2024, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.cli.mediaplayer

import nz.net.ultraq.redhorizon.engine.input.KeyControl
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.engine.scenegraph.nodes.Sound
import nz.net.ultraq.redhorizon.engine.scripting.Script

import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE

/**
 * A script to control playback of a sound effect.  This script will stream the
 * data to the sound during playback.
 *
 * @author Emanuel Rabina
 */
class SoundTrackScript extends Script<Sound> {

	@Delegate
	private Sound applyDelegate() {
		return scriptable
	}

	@Override
	void onSceneAdded(Scene scene) {

		scene.inputEventStream.addControl(new KeyControl(GLFW_KEY_SPACE, 'Play/Pause', { ->
			if (!playing || paused) {
				play()
			}
			else if (!paused) {
				pause()
			}
		}))

		play()
	}
}
