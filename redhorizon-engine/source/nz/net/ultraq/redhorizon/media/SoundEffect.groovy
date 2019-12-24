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

import nz.net.ultraq.redhorizon.engine.audio.AudioElement
import nz.net.ultraq.redhorizon.engine.audio.AudioRenderer
import nz.net.ultraq.redhorizon.filetypes.SoundFile
import nz.net.ultraq.redhorizon.filetypes.Streaming
import nz.net.ultraq.redhorizon.filetypes.Worker
import nz.net.ultraq.redhorizon.scenegraph.Movable
import nz.net.ultraq.redhorizon.scenegraph.SelfVisitable

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
class SoundEffect implements AudioElement, Movable, Playable, SelfVisitable {

	// Sound information
	final int bitrate
	final int channels
	final int frequency
	private final Worker soundDataWorker
	private final BlockingQueue<ByteBuffer> soundDataBuffer

	// Renderer information
	private int sourceId
	private List<Integer> bufferIds

	/**
	 * Constructor, use the data in {@code soundFile} for playing a sound effect.
	 * 
	 * @param soundFile
	 * @param executorService
	 */
	SoundEffect(SoundFile soundFile, ExecutorService executorService) {

		bitrate   = soundFile.bitrate.value
		channels  = soundFile.channels.value
		frequency = soundFile.frequency

		// TODO: Maybe move streaming to a "sound track" class?
		if (soundFile instanceof Streaming) {
			soundDataBuffer = new ArrayBlockingQueue<>(10)
			// TODO: Some kind of cached buffer so that some items don't need to be decoded again
			soundDataWorker = soundFile.getStreamingDataWorker { sampleBuffer ->
				soundDataBuffer << sampleBuffer
			}
			executorService.execute(soundDataWorker)
		}
	}

	@Override
	void delete(AudioRenderer renderer) {

		soundDataWorker.stop()
		soundDataBuffer.drain()
		renderer.deleteSource(sourceId)
		renderer.deleteBuffers(*bufferIds)
	}

	@Override
	void init(AudioRenderer renderer) {

		if (!renderer.sourceExists(sourceId)) {
			sourceId = renderer.createSource()
			bufferIds = []
		}
	}

	@Override
	void render(AudioRenderer renderer) {

		if (playing) {

			// Buffers to read and queue
			if (!soundDataBuffer.empty) {
				def bufferId = renderer.createBuffer(ByteBuffer.fromBuffersDirect(soundDataBuffer.drain()), bitrate, channels, frequency)
				bufferIds << bufferId
				renderer.queueBuffer(sourceId, bufferId)
			}

			// Start playing the source
//			renderer.updateSource(sourceId, position, direction, velocity)
			if (!renderer.sourcePlaying(sourceId)) {
				renderer.playSource(sourceId)
			}

			// Source finished
			if (!renderer.sourcePlaying(sourceId)) {
				stop()
			}

			// Delete played buffers as the track progresses to free up memory
			if (soundDataBuffer != null) {
				def buffersProcessed = renderer.buffersProcessed(sourceId)
				if (buffersProcessed) {
					def processedBufferIds = bufferIds.subList(0, buffersProcessed)
					renderer.unqueueBuffers(sourceId, *processedBufferIds)
					renderer.deleteBuffers(*processedBufferIds)
					bufferIds -= processedBufferIds
				}
			}
		}
	}
}
