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

package nz.net.ultraq.redhorizon.media

import nz.net.ultraq.redhorizon.engine.audio.AudioElement
import nz.net.ultraq.redhorizon.engine.audio.AudioRenderer
import nz.net.ultraq.redhorizon.filetypes.SoundFile
import nz.net.ultraq.redhorizon.scenegraph.Movable
import nz.net.ultraq.redhorizon.scenegraph.SceneElement

import java.nio.ByteBuffer

/**
 * A simple piece of short audio that can be loaded entirely into memory for
 * multiple playbacks.
 * 
 * @author Emanuel Rabina
 */
class SoundEffect implements AudioElement, Movable, Playable, SceneElement {

	// Sound information
	final int bits
	final int channels
	final int frequency
	ByteBuffer buffer

	// Renderer information
	private int sourceId
	private int bufferId

	/**
	 * Constructor, use the data in {@code soundFile} for playing the sound
	 * effect.
	 * 
	 * @param soundFile
	 */
	SoundEffect(SoundFile soundFile) {

		bits      = soundFile.bits
		channels  = soundFile.channels
		frequency = soundFile.frequency
		buffer    = soundFile.soundData
	}

	@Override
	void delete(AudioRenderer renderer) {

		renderer.deleteSource(sourceId)
		renderer.deleteBuffers(bufferId)
	}

	@Override
	void init(AudioRenderer renderer) {

		sourceId = renderer.createSource()
		bufferId = renderer.createBuffer(buffer, bits, channels, frequency)
		renderer.attachBufferToSource(sourceId, bufferId)
		buffer = null
	}

	@Override
	void render(AudioRenderer renderer) {

		if (playing) {
			// Buffer exhausted
			if (renderer.sourceStopped(sourceId)) {
				stop()
			}
			// Start playing the source
			else if (!renderer.sourcePlaying(sourceId)) {
				renderer.playSource(sourceId)
			}
		}
		else {
			// Sound stopped, but source still playing
			if (renderer.sourcePlaying(sourceId)) {
				stop()
			}
		}
	}
}
