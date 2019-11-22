/* 
 * Copyright 2007, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import nz.net.ultraq.redhorizon.engine.AudioElement
import nz.net.ultraq.redhorizon.engine.audio.AudioRenderer
import nz.net.ultraq.redhorizon.filetypes.SoundFile
import nz.net.ultraq.redhorizon.filetypes.Worker
import nz.net.ultraq.redhorizon.scenegraph.Movable
import nz.net.ultraq.redhorizon.scenegraph.SceneElement
import nz.net.ultraq.redhorizon.scenegraph.SceneVisitor

import java.nio.ByteBuffer
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ExecutorService

/**
 * Basic sound in a 3D space.  Sound effects are constructed from data in
 * {@link SoundFile}s, which can then be played through the audio engine.
 * 
 * @author Emanuel Rabina
 */
class SoundEffect extends Media implements AudioElement, Movable, Playable, SceneElement {

	// Sound information
	final int bitrate
	final int channels
	final int frequency
	private final Worker soundDataWorker
	private BlockingQueue<ByteBuffer> soundDataBuffer = new ArrayBlockingQueue<>(10)

	// Renderer information
	private int sourceId
	private List<Integer> bufferIds

	/**
	 * Constructor, loads the sound from the given <code>SoundFile</code>.
	 * 
	 * @param soundFile File which should be used to construct this sound.
	 * @param executor
	 */
	SoundEffect(SoundFile soundFile, ExecutorService executorService) {

		super(soundFile.filename)
		bitrate   = soundFile.bitrate.value
		channels  = soundFile.channels.value
		frequency = soundFile.frequency

		soundDataWorker = soundFile.getSoundDataWorker(executorService)
		// TODO: Some kind of cached buffer so that some items don't need to be decoded again
		soundDataWorker.work { chunkBuffer ->
			soundDataBuffer << chunkBuffer
		}
	}

	/**
	 * @inheritDoc
	 */
	@Override
	void accept(SceneVisitor visitor) {

		visitor.visit(this)
	}

	/**
	 * @inheritDoc
	 */
	@Override
	void delete(AudioRenderer renderer) {

		renderer.deleteSource(sourceId)
		renderer.deleteBuffers(bufferIds as int[])
	}

	/**
	 * @inheritDoc
	 */
	@Override
	void init(AudioRenderer renderer) {

		if (!renderer.sourceExists(sourceId)) {
			sourceId = renderer.createSource()
			bufferIds = []
		}
	}

	/**
	 * @inheritDoc
	 */
	@Override
	void render(AudioRenderer renderer) {

		if (playing) {

			// Buffers to read and queue
			if (!soundDataBuffer.empty) {
				def nextBuffer = soundDataBuffer.take()
				def bufferId = renderer.createBuffer(nextBuffer, bitrate, channels, frequency)
				bufferIds << bufferId
				renderer.queueBuffer(sourceId, bufferId)
//				renderer.updateSource(sourceId, position, direction, velocity)

				// Start playing the source
				if (!renderer.sourcePlaying(sourceId)) {
					renderer.playSource(sourceId)
				}
			}

			// No more buffers to read
			else if (soundDataWorker.complete) {
				if (!renderer.sourcePlaying(sourceId)) {
					playing = false
				}
			}
		}
	}
}
