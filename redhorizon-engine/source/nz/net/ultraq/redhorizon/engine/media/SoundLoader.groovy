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
import nz.net.ultraq.redhorizon.engine.input.InputEventStream
import nz.net.ultraq.redhorizon.engine.input.KeyEvent
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.engine.scenegraph.SceneElement
import nz.net.ultraq.redhorizon.events.EventListener
import nz.net.ultraq.redhorizon.filetypes.SoundFile
import nz.net.ultraq.redhorizon.filetypes.Streaming

import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE
import static org.lwjgl.glfw.GLFW.GLFW_PRESS

/**
 * Load a sound effect or music track into existing engines.
 * 
 * @author Emanuel Rabina
 */
class SoundLoader extends MediaLoader<SoundFile, Playable> implements EventListener<KeyEvent> {

	final GameClock gameClock
	final InputEventStream inputEventStream

	/**
	 * Constructor, create a loader for sound files.
	 *
	 * @param soundFile
	 * @param scene
	 * @param gameClock
	 * @param inputEventStream
	 */
	SoundLoader(SoundFile soundFile, Scene scene, GameClock gameClock, InputEventStream inputEventStream) {

		super(soundFile, scene)
		this.gameClock = gameClock
		this.inputEventStream = inputEventStream
	}

	@Override
	void handleEvent(KeyEvent event) {

		if (event.action == GLFW_PRESS) {
			switch (event.key) {
				case GLFW_KEY_SPACE:
					gameClock.togglePause()
					break
			}
		}
	}

	@Override
	Playable load() {

		media = file.forStreaming ? new SoundTrack(file, gameClock) : new SoundEffect(file)
		scene << media
		inputEventStream.on(KeyEvent, this)

		return media
	}

	@Override
	void unload() {

		if (media.playing) {
			media.stop()
			if (file instanceof Streaming) {
				file.streamingDataWorker.stop()
			}
		}
		inputEventStream.off(KeyEvent, this)
		scene.removeSceneElement((SceneElement)media)
	}
}
