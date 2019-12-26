/* 
 * Copyright 2019, Emanuel Rabina (http://www.ultraq.net.nz/)
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
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsElement
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.filetypes.Streaming
import nz.net.ultraq.redhorizon.filetypes.VideoFile
import nz.net.ultraq.redhorizon.filetypes.Worker
import nz.net.ultraq.redhorizon.scenegraph.SelfVisitable

import org.joml.Rectanglef
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.ByteBuffer
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ExecutorService

/**
 * The combination of an animation and sound track, a video is a stream from a
 * video file, rendering out to both the audio and graphics engines during
 * playback.
 * 
 * @author Emanuel Rabina
 */
class Video implements AudioElement, GraphicsElement, Playable, SelfVisitable {

	private static final Logger logger = LoggerFactory.getLogger(Video)

	// TODO: Can a video be made up of an animation and soundtrack, delegating to
	//       the 2 so that I don't have to repeat code here?

	// Video file attributes
	final int width
	final int height
	final int format
	final int numFrames
	final float frameRate
	final int bitrate
	final int channels
	final int frequency
	final Rectanglef dimensions

	private long animationTimeStart
	private final Worker videoWorker
	private final BlockingQueue<ByteBuffer> frameDataBuffer
	private final BlockingQueue<ByteBuffer> sampleDataBuffer

	// Rendering information
	private List<ByteBuffer> frames // TODO: This still keeps frames around in memory 🤔
	private int lastFrame
	private List<Integer> textureIds

	private int sourceId
	private List<Integer> bufferIds

	/**
	 * Constructor, creates a video out of video file data.
	 * 
	 * @param videoFile       Video source.
	 * @param dimensions      Dimensions over which to display the video over.
	 * @param executorService
	 */
	Video(VideoFile videoFile, Rectanglef dimensions, ExecutorService executorService) {

		width     = videoFile.width
		height    = videoFile.height
		format    = videoFile.format.value
		numFrames = videoFile.numFrames
		frameRate = videoFile.frameRate
		this.dimensions = dimensions

		bitrate   = videoFile.bitrate.value
		channels  = videoFile.channels.value
		frequency = videoFile.frequency

		if (videoFile instanceof Streaming) {
			frameDataBuffer = new ArrayBlockingQueue<>(frameRate * 3 as int)
			sampleDataBuffer = new ArrayBlockingQueue<>(frameRate * 3 as int)
			// TODO: Some kind of cached buffer so that some items don't need to be decoded again
			videoWorker = videoFile.getStreamingDataWorker { frame, sample ->
				if (frame) {
					frameDataBuffer << ByteBuffer.fromBuffersDirect(frame)
				}
				if (sample) {
					sampleDataBuffer << ByteBuffer.fromBuffersDirect(sample)
				}
			}
			executorService.execute(videoWorker)
		}
		else {
			throw new UnsupportedOperationException(
				'Video file doesn\'t support streaming - loading all the raw data isn\'t good for memory usage!')
		}
	}

	@Override
	void delete(AudioRenderer renderer) {

		videoWorker.stop()
		sampleDataBuffer.drain()
		renderer.deleteSource(sourceId)
		renderer.deleteBuffers(*bufferIds)
	}

	@Override
	void delete(GraphicsRenderer renderer) {

		videoWorker.stop()
		frameDataBuffer.drain()
		renderer.deleteTextures(*(textureIds.findAll { textureId -> !textureId }))
	}

	@Override
	void init(AudioRenderer renderer) {

		if (!renderer.sourceExists(sourceId)) {
			sourceId = renderer.createSource()
			bufferIds = []
		}
	}

	@Override
	void init(GraphicsRenderer renderer) {

		frames = []
		lastFrame = -1
		textureIds = [].withDefault { null as Integer }
	}

	@Override
	void play() {

		animationTimeStart = System.currentTimeMillis()
		Playable.super.play()
	}

	@Override
	void render(AudioRenderer renderer) {

		if (playing) {

			// Buffers to read and queue
			if (!sampleDataBuffer.empty) {
				def newBufferIds = sampleDataBuffer.drain().collect { buffer ->
					def newBufferId = renderer.createBuffer(buffer, bitrate, channels, frequency)
					bufferIds << newBufferId
					return newBufferId
				}
				renderer.queueBuffers(sourceId, *newBufferIds)

				// Start playing the source
//				renderer.updateSource(sourceId, position, direction, velocity)
				if (!renderer.sourcePlaying(sourceId)) {
					renderer.playSource(sourceId)
				}
			}

			// No more buffers to read, wait for the source to complete
			else if (videoWorker.complete) {
				if (!renderer.sourcePlaying(sourceId)) {
					stop()
				}
			}
		}

		// Delete played buffers as the track progresses to free up memory
		if (sampleDataBuffer != null) {
			def buffersProcessed = renderer.buffersProcessed(sourceId)
			if (buffersProcessed) {
				def processedBufferIds = buffersProcessed.collect { bufferIds.removeAt(0) }
				renderer.unqueueBuffers(sourceId, *processedBufferIds)
				renderer.deleteBuffers(*processedBufferIds)
			}
		}
	}

	@Override
	void render(GraphicsRenderer renderer) {

		if (playing) {

			// Frames to load
			if (!frameDataBuffer.empty) {
				frames.addAll(frameDataBuffer.drain())
			}

			def currentFrame = Math.floor((System.currentTimeMillis() - animationTimeStart) / 1000 * frameRate) as int

			// Draw the current frame if available
			if (currentFrame < numFrames) {
				def textureId = textureIds[currentFrame]
				if (!textureId) {
					def frame = frames[currentFrame]
					if (frame) {
						textureId = renderer.createTexture(frame, format, width, height)
						renderer.drawTexture(textureId, dimensions)
						textureIds[currentFrame] = textureId
					}
					else {
						logger.debug('Frame {} not available, skipping', currentFrame)
					}
				}
				else {
					renderer.drawTexture(textureId, dimensions)
				}
			}

			// Video over
			else {
				stop()
			}

			// Delete used frames as the video progresses to free up memory
//			if (lastFrame != -1 && lastFrame != currentFrame) {
//				System.out.println("Deleting frame: ${lastFrame}")
//				def lastTextureId = textureIds[lastFrame]
//				if (lastTextureId) {
//					renderer.deleteTextures(lastTextureId)
//				}
//				lastFrame..<currentFrame.each { i ->
//					frames[i] = null
//				}
//			}
//			lastFrame = currentFrame
		}
	}
}
