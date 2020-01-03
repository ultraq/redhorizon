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
import nz.net.ultraq.redhorizon.scenegraph.SelfVisitable

import java.nio.ByteBuffer
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ExecutorService

/**
 * A long piece of audio that is streamed from its source.
 * 
 * @author Emanuel Rabina
 */
class SoundTrack implements AudioElement, Playable, SelfVisitable {

	// Sound information
	final int bits
	final int channels
	final int frequency

	private final Worker soundDataWorker
	private final int sampleBufferTarget = 10
	private final BlockingQueue<ByteBuffer> sampleBuffer = new ArrayBlockingQueue<>(sampleBufferTarget)
	private int samplesQueued

	// Renderer information
	private int sourceId
	private List<Integer> bufferIds

	/**
	 * Constructor, use the data in {@code soundFile} for playing the sound track.
	 * 
	 * @param soundFile
	 * @param executorService
	 */
	SoundTrack(SoundFile soundFile, ExecutorService executorService) {

		bits      = soundFile.bits
		channels  = soundFile.channels
		frequency = soundFile.frequency

		if (soundFile instanceof Streaming) {
			// TODO: Some kind of cached buffer so that some items don't need to be decoded again
			soundDataWorker = soundFile.getStreamingDataWorker { sample ->
				sampleBuffer << ByteBuffer.fromBuffersDirect(sample)
			}
			executorService.execute(soundDataWorker)
		}
		else {
			throw new UnsupportedOperationException('The SoundTrack media type should only be used for streaming audio')
		}
	}

	@Override
	void delete(AudioRenderer renderer) {

		soundDataWorker.stop()
		sampleBuffer.drain()
		renderer.deleteSource(sourceId)
		renderer.deleteBuffers(*bufferIds)
	}

	@Override
	void init(AudioRenderer renderer) {

		if (!renderer.sourceExists(sourceId)) {
			sourceId = renderer.createSource()
		}
		bufferIds = []
		samplesQueued = 0
	}

	@Override
	void render(AudioRenderer renderer) {

		if (playing) {

			// Buffers to read and queue
			if (sampleBuffer.size()) {
				def numBuffersToRead = sampleBufferTarget - samplesQueued
				if (numBuffersToRead) {
					def newBufferIds = sampleBuffer.drain(numBuffersToRead).collect { buffer ->
						def newBufferId = renderer.createBuffer(buffer, bits, channels, frequency)
						bufferIds << newBufferId
						return newBufferId
					}
					renderer.queueBuffers(sourceId, *newBufferIds)
					samplesQueued += newBufferIds.size()
				}
			}

			// Buffers exhausted
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

		// Delete played buffers as the track progresses to free up memory
		if (samplesQueued) {
			def buffersProcessed = renderer.buffersProcessed(sourceId)
			if (buffersProcessed) {
				def processedBufferIds = []
				buffersProcessed.times {
					processedBufferIds << bufferIds.removeAt(0)
				}
				renderer.unqueueBuffers(sourceId, *processedBufferIds)
				renderer.deleteBuffers(*processedBufferIds)
				samplesQueued -= buffersProcessed
			}
		}
	}
}
