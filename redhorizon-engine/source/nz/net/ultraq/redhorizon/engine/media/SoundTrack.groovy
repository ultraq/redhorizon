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

package nz.net.ultraq.redhorizon.engine.media

import nz.net.ultraq.redhorizon.engine.audio.AudioElement
import nz.net.ultraq.redhorizon.engine.audio.AudioRenderer
import nz.net.ultraq.redhorizon.engine.scenegraph.SceneElement
import nz.net.ultraq.redhorizon.engine.time.Temporal
import nz.net.ultraq.redhorizon.filetypes.SoundFile
import nz.net.ultraq.redhorizon.filetypes.Streaming
import nz.net.ultraq.redhorizon.filetypes.StreamingSampleEvent
import nz.net.ultraq.redhorizon.filetypes.Worker

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.transform.PackageScope
import java.nio.ByteBuffer
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

/**
 * A long piece of audio that is streamed from its source.
 *
 * @author Emanuel Rabina
 */
@SuppressWarnings('GrFinalVariableAccess')
class SoundTrack implements AudioElement, Playable, SceneElement, Temporal {

	private static final Logger logger = LoggerFactory.getLogger(SoundTrack)

	// Sound information
	final int bits
	final int channels
	final int frequency

	private final Worker soundDataWorker
	private final BlockingQueue<ByteBuffer> samples
	private final int bufferSize
	private final CountDownLatch bufferReady = new CountDownLatch(1)

	// Renderer information
	private int sourceId
	private List<Integer> bufferIds
	private boolean paused

	/**
	 * Constructor, use the data in {@code soundFile} for playing the sound track.
	 *
	 * @param soundFile
	 */
	SoundTrack(SoundFile soundFile) {

		this(soundFile.bits, soundFile.channels, soundFile.frequency,
			soundFile instanceof Streaming ? soundFile.streamingDataWorker : null)

		Executors.newSingleThreadExecutor().execute(soundDataWorker)
	}

	/**
	 * Constructor, use all of the given data for building a sound track.
	 *
	 * @param bits
	 * @param channels
	 * @param frequency
	 * @param bufferSize
	 * @param soundDataWorker
	 */
	@PackageScope
	SoundTrack(int bits, int channels, int frequency, int bufferSize = 10, Worker soundDataWorker) {

		if (!soundDataWorker) {
			throw new UnsupportedOperationException('Streaming configuration used, but source doesn\'t support streaming')
		}

		this.bits = bits
		this.channels = channels
		this.frequency = frequency

		samples = new ArrayBlockingQueue<>(bufferSize)
		this.bufferSize = bufferSize
		this.soundDataWorker = soundDataWorker
		this.soundDataWorker.on(StreamingSampleEvent) { event ->
			samples << event.sample
			if (bufferReady.count && !samples.remainingCapacity()) {
				bufferReady.countDown()
			}
		}
	}

	@Override
	void delete(AudioRenderer renderer) {

		soundDataWorker.stop()
		samples.drain()
		renderer.deleteSource(sourceId)
		renderer.deleteBuffers(bufferIds as int[])
	}

	@Override
	void init(AudioRenderer renderer) {

		sourceId = renderer.createSource()
		bufferIds = []
	}

	@Override
	void play() {

		// Wait until the sample buffer is filled before starting play
		bufferReady.await()
		Playable.super.play()
	}

	@Override
	void render(AudioRenderer renderer) {

		if (playing) {

			// Buffers to read and queue, maxing at 5 so we don't spend too much time in here
			if (samples.size()) {
				logger.trace('Samples available: {}', samples.size())
				def newBufferIds = samples.drain(5).collect { buffer ->
					def newBufferId = renderer.createBuffer(buffer, bits, channels, frequency)
					bufferIds << newBufferId
					return newBufferId
				}
				renderer.queueBuffers(sourceId, newBufferIds as int[])
				logger.trace('Increasing buffersQueued by {}', newBufferIds.size())
			}

			// Pause/play with game time
			if (paused) {
				if (!renderer.sourcePaused(sourceId)) {
					renderer.pauseSource(sourceId)
				}
			}
			else {
				// Buffers exhausted
				if (renderer.sourceStopped(sourceId)) {
					stop()
				}
				// Start playing the source
				else if (!renderer.sourcePlaying(sourceId)) {
					renderer.playSource(sourceId)
				}
			}
		}
		else {
			// Sound stopped, but source still playing
			if (renderer.sourcePlaying(sourceId)) {
				renderer.stopSource(sourceId)
			}
		}

		// Delete played buffers as the track progresses to free up memory
		def buffersProcessed = renderer.buffersProcessed(sourceId)
		logger.trace('Buffers processed: {}', buffersProcessed)
		if (buffersProcessed) {
			def processedBufferIds = []
			buffersProcessed.times {
				processedBufferIds << bufferIds.removeAt(0)
			}
			renderer.unqueueBuffers(sourceId, processedBufferIds as int[])
			renderer.deleteBuffers(processedBufferIds as int[])
			logger.trace('Decreasing buffersQueued by {}', buffersProcessed)
		}
	}

	@Override
	void tick(long updatedTimeMs) {

		paused = currentTimeMs == updatedTimeMs
		currentTimeMs = updatedTimeMs
	}
}
