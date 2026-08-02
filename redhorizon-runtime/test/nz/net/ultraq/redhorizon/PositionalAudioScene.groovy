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

package nz.net.ultraq.redhorizon

import nz.net.ultraq.redhorizon.audio.AudioStoppedEvent
import nz.net.ultraq.redhorizon.audio.Sound
import nz.net.ultraq.redhorizon.audio.SourceNode
import nz.net.ultraq.redhorizon.engine.graphics.GridLines
import nz.net.ultraq.redhorizon.engine.scripts.Script
import nz.net.ultraq.redhorizon.engine.scripts.ScriptNode
import nz.net.ultraq.redhorizon.graphics.Camera
import nz.net.ultraq.redhorizon.graphics.imgui.DebugOverlay
import nz.net.ultraq.redhorizon.runtime.Application
import nz.net.ultraq.redhorizon.scenegraph.Scene
import static nz.net.ultraq.redhorizon.runtime.ScopedValues.*

import org.joml.Vector3f

/**
 * A simple application for testing positional audio.
 *
 * @author Emanuel Rabina
 */
class PositionalAudioScene extends Application {

	PositionalAudioScene() {

		super('Positional audio', 'test')
	}

	@Override
	Scene configureScene(Scene scene) {

		scene.find(GridLines).enable()
		scene.find(DebugOverlay).enable()
		return scene
			.addChild(new ScriptNode(PositionalAudioScript))
	}

	static class PositionalAudioScript extends Script {

		private final Vector3f worldCoords = new Vector3f()
		private float cooldown = 0f
		private Sound sound

		@Override
		void init() {

			var resourceManager = RESOURCE_MANAGER.get()
			sound = resourceManager.loadSound('PositionalAudioScene_bong_001.ogg')
		}

		@Override
		void update(float delta) {

			cooldown += delta
			if (cooldown > 1f) {
				var window = WINDOW.get()

				var camera = node.scene.find(Camera)
				var cursorPosition = input.cursorPosition()
				camera.unproject(window.viewport, cursorPosition.x(), cursorPosition.y(), worldCoords)

				// TODO: Play sound once utility?
				var scene = node.scene
				scene.queueUpdate { ->
					var impactSoundSource = scene.addAndReturnChild(
						new SourceNode(sound)
							.setPosition(worldCoords.x(), worldCoords.y(), 0f)
							.withRolloff(0f)
							.withName('Cursor position sound')
					)
					impactSoundSource
						.on(AudioStoppedEvent) { stoppedEvent ->
							scene.queueUpdate { ->
								impactSoundSource.remove()
							}
						}
						.play()
				}

				cooldown -= 1f
			}
		}
	}
}
