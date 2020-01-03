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

import nz.net.ultraq.redhorizon.engine.graphics.GraphicsElement
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.filetypes.AnimationFile
import nz.net.ultraq.redhorizon.filetypes.Streaming
import nz.net.ultraq.redhorizon.filetypes.Worker
import nz.net.ultraq.redhorizon.scenegraph.SelfVisitable

import org.joml.Rectanglef
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.ByteBuffer
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService

/**
 * An animation that plays a series of images at a certain speed.
 * 
 * @author Emanuel Rabina
 */
class Animation implements GraphicsElement, Playable, SelfVisitable {

	private static final Logger logger = LoggerFactory.getLogger(Animation)

	// Animation file attributes
	final int width
	final int height
	final int format
	final int numFrames
	final float frameRate
	final Rectanglef dimensions
	final boolean filter

	private final Worker frameDataWorker
	private final int frameBufferTarget
	private final BlockingQueue<ByteBuffer> frameBuffer
	private final CountDownLatch bufferReady = new CountDownLatch(1)
	private long animationTimeStart

	// Rendering information
	private List<ByteBuffer> frames
	private int lastFrame
	private int[] textureIds

	/**
	 * Constructor, create an animation out of animation file data.
	 * 
	 * @param animationFile   Animation source.
	 * @param dimensions      Dimensions over which to display the animation over.
	 * @param filter          Filter the frames of the animation.
	 * @param executorService
	 */
	Animation(AnimationFile animationFile, Rectanglef dimensions, boolean filter, ExecutorService executorService) {

		width     = animationFile.width
		height    = animationFile.height
		format    = animationFile.format.value
		numFrames = animationFile.numFrames
		frameRate = animationFile.frameRate
		this.dimensions = dimensions
		this.filter = filter

		if (animationFile instanceof Streaming) {
			frameBufferTarget = frameRate as int
			frameBuffer = new ArrayBlockingQueue<>(frameBufferTarget)
			// TODO: Some kind of cached buffer so that some items don't need to be decoded again
			frameDataWorker = animationFile.getStreamingDataWorker { frame ->
				if (!frameBuffer.remainingCapacity() && bufferReady.count) {
					bufferReady.countDown()
				}
				frameBuffer << ByteBuffer.fromBuffersDirect(frame)
			}
			executorService.execute(frameDataWorker)
		}
		else {
			throw new UnsupportedOperationException(
				'Only streaming animations currently supported - loading all the frame data isn\'t good for memory usage!')
		}
	}

	@Override
	void delete(GraphicsRenderer renderer) {

		frameDataWorker.stop()
		frameBuffer.drain()
		renderer.deleteTextures(textureIds.findAll { it } as int[])
	}

	@Override
	void init(GraphicsRenderer renderer) {

		frames = []
		lastFrame = -1
		textureIds = new int[numFrames]
		Arrays.fill(textureIds, 0)
	}

	@Override
	void play() {

		// Wait until the frame buffer is filled before starting play
		bufferReady.await()
		animationTimeStart = System.currentTimeMillis()
		Playable.super.play()
	}

	@Override
	void render(GraphicsRenderer renderer) {

		if (playing) {
			def currentFrame = Math.floor((System.currentTimeMillis() - animationTimeStart) / 1000 * frameRate) as int

			// Try to load up to frameBufferTarget frames ahead
			if (frameBuffer.size()) {
				def framesAhead = Math.min(currentFrame + frameBufferTarget, numFrames)
				if (!frames[framesAhead]) {
					def numFramesToRead = framesAhead - frames.size()
					if (numFramesToRead) {
						def newFrames = frameBuffer.drain(numFramesToRead)
						frames.addAll(newFrames)
					}
				}
			}

			// Draw the current frame if available
			if (currentFrame < numFrames) {
				def textureId = textureIds[currentFrame]
				if (!textureId) {
					def frame = frames[currentFrame]
					if (frame) {
						textureId = renderer.createTexture(frame, format, width, height, filter)
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

			// Animation over
			else {
				stop()
			}

			// Delete used frames as the animation progresses to free up memory
			if (lastFrame != -1 && lastFrame != currentFrame) {
				def lastTextureId = textureIds[lastFrame]
				if (lastTextureId) {
					renderer.deleteTextures(lastTextureId)
				}
				lastFrame..<currentFrame.each { i ->
					frames[i] = null
				}
			}
			lastFrame = currentFrame
		}
	}
}
