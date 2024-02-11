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

import nz.net.ultraq.redhorizon.engine.Application
import nz.net.ultraq.redhorizon.engine.input.KeyControl
import nz.net.ultraq.redhorizon.engine.media.Playable
import nz.net.ultraq.redhorizon.engine.media.StopEvent
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.engine.scenegraph.scripting.Script

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE

import groovy.transform.TupleConstructor

/**
 * A script to control playback of a sound effect or track.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor
class PlaybackScript extends Script {

	private static final Logger logger = LoggerFactory.getLogger(PlaybackScript)

	final Application application
	final boolean runOnce

	@Delegate
	private Playable applyDelegate() {
		return scriptable as Playable
	}

	@Override
	void onSceneAdded(Scene scene) {

		scene.inputEventStream.addControl(new KeyControl(GLFW_KEY_SPACE, 'Play/Pause', { ->
			if (runOnce) {
				logger.debug('Pausing/Resuming playback')
				scene.gameClock.togglePause()
			}
			else {
				if (!playing || paused) {
					logger.debug('Playing')
					play()
				}
			}
		}))

		Thread.sleep(1000)
		logger.debug('Beginning playback')
		play()

		on(StopEvent) { event ->
			if (runOnce) {
				logger.debug('Playback complete and script configured for runOnce behaviour - shutting down')
				application.stop()
			}
			else {
				logger.debug('Playback complete')
			}
		}
	}
}
