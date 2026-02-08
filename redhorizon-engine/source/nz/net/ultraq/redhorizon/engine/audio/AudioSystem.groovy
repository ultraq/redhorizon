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

package nz.net.ultraq.redhorizon.engine.audio

import nz.net.ultraq.redhorizon.audio.AudioNode
import nz.net.ultraq.redhorizon.engine.System
import nz.net.ultraq.redhorizon.scenegraph.Scene

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Render audio nodes.
 *
 * @author Emanuel Rabina
 */
class AudioSystem extends System {

	private static final Logger logger = LoggerFactory.getLogger(AudioSystem)

	private final List<AudioNode> audioNodes = new ArrayList<>()

	@Override
	void update(Scene scene, float delta) {

		average('Update', 1f, logger) { ->
			audioNodes.clear()
			scene.traverse(AudioNode) { AudioNode audio ->
				audioNodes << audio
				return true
			}
			audioNodes.each { audio ->
				if (audio.enabled) {
					audio.render()
				}
			}
		}
	}
}
