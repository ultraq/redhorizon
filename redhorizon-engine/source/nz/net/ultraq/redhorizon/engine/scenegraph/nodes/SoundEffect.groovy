/*
 * Copyright 2020, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine.scenegraph.nodes

import nz.net.ultraq.redhorizon.engine.audio.AudioElement
import nz.net.ultraq.redhorizon.engine.audio.AudioRenderer
import nz.net.ultraq.redhorizon.engine.audio.AudioRequests.BufferRequest
import nz.net.ultraq.redhorizon.engine.audio.AudioRequests.SourceRequest
import nz.net.ultraq.redhorizon.engine.audio.Buffer
import nz.net.ultraq.redhorizon.engine.audio.Source
import nz.net.ultraq.redhorizon.engine.media.Playable
import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.filetypes.SoundFile

import groovy.transform.TupleConstructor

/**
 * A simple piece of short audio that can be loaded entirely into memory for
 * multiple playbacks.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(includes = ['soundFile'])
class SoundEffect implements Node<SoundEffect>, AudioElement, Playable {

	final SoundFile soundFile

	private Source source
	private Buffer buffer

	@Override
	void delete(AudioRenderer renderer) {
	}

	@Override
	void init(AudioRenderer renderer) {
	}

	@Override
	void onSceneAdded(Scene scene) {

		source = scene
			.requestCreateOrGet(new SourceRequest())
			.get()
		buffer = scene
			.requestCreateOrGet(new BufferRequest(soundFile.bits, soundFile.channels, soundFile.frequency, soundFile.soundData))
			.get()
	}

	@Override
	void onSceneRemoved(Scene scene) {

		scene.requestDelete(buffer, source)
	}

	@Override
	void render(AudioRenderer renderer) {

		if (!source || !buffer) {
			return
		}

		source.attachBuffer(buffer)

		if (playing) {
			if (source.stopped) {
				source.rewind()
				stop()
			}
			else if (!source.playing) {
				source.play()
			}
		}
		else {
			if (source.playing) {
				source.stop()
			}
		}
	}
}
