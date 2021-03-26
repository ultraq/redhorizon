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

import nz.net.ultraq.redhorizon.engine.GameTime
import nz.net.ultraq.redhorizon.engine.audio.AudioElement
import nz.net.ultraq.redhorizon.engine.audio.AudioRenderer
import nz.net.ultraq.redhorizon.filetypes.SoundFile
import nz.net.ultraq.redhorizon.filetypes.Streaming
import nz.net.ultraq.redhorizon.filetypes.StreamingSampleEvent
import nz.net.ultraq.redhorizon.filetypes.Worker
import nz.net.ultraq.redhorizon.scenegraph.SceneElement

import groovy.transform.PackageScope
import java.nio.ByteBuffer
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService

/**
 * A long piece of audio that is streamed from its source.
 * 
 * @author Emanuel Rabina
 */
class SoundTrack implements AudioElement, Playable, SceneElement {

	// Sound information
	final int bits
	final int channels
	final int frequency

	private final Worker soundDataWorker
	private final BlockingQueue<ByteBuffer> samples
	private final int bufferSize
	private final CountDownLatch bufferReady = new CountDownLatch(1)
	private int buffersQueued
	private GameTime gameTime

	// Renderer information
	private int sourceId
	private List<Integer> bufferIds

	/**
	 * Constructor, use the data in {@code soundFile} for playing the sound track.
	 * 
	 * @param soundFile
	 * @param gameTime
	 * @param executorService
	 */
	SoundTrack(SoundFile soundFile, GameTime gameTime, ExecutorService executorService) {

		this(soundFile.bits, soundFile.channels, soundFile.frequency,
			soundFile instanceof Streaming ? soundFile.streamingDataWorker : null, gameTime)

		executorService.execute(soundDataWorker)
	}

	/**
	 * Constructor, use all of the given data for building a sound track.
	 * 
	 * @param bits
	 * @param channels
	 * @param frequency
	 * @param bufferSize
	 * @param soundDataWorker
	 * @param gameTime
	 */
	@PackageScope
	SoundTrack(int bits, int channels, int frequency, int bufferSize = 10, Worker soundDataWorker, GameTime gameTime) {

		if (!soundDataWorker) {
			throw new UnsupportedOperationException('Streaming configuration used, but source doesn\'t support streaming')
		}

		this.bits      = bits
		this.channels  = channels
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

		this.gameTime = gameTime
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

		if (!renderer.sourceExists(sourceId)) {
			sourceId = renderer.createSource()
		}
		bufferIds = []
		buffersQueued = 0
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
				def numBuffersToRead = bufferSize - buffersQueued
				if (numBuffersToRead) {
					def newBufferIds = samples.drain(Math.max(numBuffersToRead, 5)).collect { buffer ->
						def newBufferId = renderer.createBuffer(buffer, bits, channels, frequency)
						bufferIds << newBufferId
						return newBufferId
					}
					renderer.queueBuffers(sourceId, newBufferIds as int[])
					buffersQueued += newBufferIds.size()
				}
			}

			// Pause/play with game time
			if (gameTime.paused) {
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
				stop()
			}
		}

		// Delete played buffers as the track progresses to free up memory
		if (buffersQueued) {
			def buffersProcessed = renderer.buffersProcessed(sourceId)
			if (buffersProcessed) {
				def processedBufferIds = []
				buffersProcessed.times {
					processedBufferIds << bufferIds.removeAt(0)
				}
				renderer.unqueueBuffers(sourceId, processedBufferIds as int[])
				renderer.deleteBuffers(processedBufferIds as int[])
				buffersQueued -= buffersProcessed
			}
		}
	}
}
