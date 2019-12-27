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
	private final BlockingQueue<ByteBuffer> soundDataBuffer = new ArrayBlockingQueue<>(10)

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

		bitrate   = soundFile.bitrate
		channels  = soundFile.channels
		frequency = soundFile.frequency

		// TODO: Maybe move streaming to a "sound track" class?
		if (soundFile instanceof Streaming) {
			// TODO: Some kind of cached buffer so that some items don't need to be decoded again
			soundDataWorker = soundFile.getStreamingDataWorker { sampleBuffer ->
				soundDataBuffer << ByteBuffer.fromBuffersDirect(sampleBuffer)
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
				def newBufferIds = soundDataBuffer.drain().collect { buffer ->
					def newBufferId = renderer.createBuffer(buffer, bitrate, channels, frequency)
					bufferIds << newBufferId
					return newBufferId
				}
				renderer.queueBuffers(sourceId, *newBufferIds)

				// Start playing the source
				if (!renderer.sourcePlaying(sourceId)) {
					renderer.playSource(sourceId)
				}
			}

			// No more buffers to read, wait for the source to complete
			if (soundDataWorker.complete) {
				if (!renderer.sourcePlaying(sourceId)) {
					stop()
				}
			}
		}

		// Delete played buffers as the track progresses to free up memory
		if (soundDataBuffer) {
			def buffersProcessed = renderer.buffersProcessed(sourceId)
			if (buffersProcessed) {
				def processedBufferIds = buffersProcessed.collect { bufferIds.removeAt(0) }
				renderer.unqueueBuffers(sourceId, *processedBufferIds)
				renderer.deleteBuffers(*processedBufferIds)
			}
		}
	}
}
