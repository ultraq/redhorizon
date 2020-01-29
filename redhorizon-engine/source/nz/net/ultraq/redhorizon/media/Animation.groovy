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
import nz.net.ultraq.redhorizon.filetypes.StreamingFrameEvent
import nz.net.ultraq.redhorizon.filetypes.Worker
import nz.net.ultraq.redhorizon.scenegraph.SelfVisitable

import org.joml.Rectanglef
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.transform.PackageScope
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

	private final Worker animationDataWorker
	private final BlockingQueue<ByteBuffer> frames
	private final int bufferSize
	private final CountDownLatch bufferReady = new CountDownLatch(1)
	private int framesQueued
	private long animationTimeStart

	// Rendering information
	private int lastFrame
	private List<Integer> textureIds

	/**
	 * Constructor, create an animation out of animation file data.
	 * 
	 * @param animationFile   Animation source.
	 * @param dimensions      Dimensions over which to display the animation over.
	 * @param filter          Filter the frames of the animation.
	 * @param scale           Double the output resolution of low-resolution
	 *                        animations.
	 * @param executorService
	 */
	Animation(AnimationFile animationFile, Rectanglef dimensions, boolean filter, boolean scale, ExecutorService executorService) {

		this(animationFile.width, animationFile.height, animationFile.format.value, animationFile.numFrames, animationFile.frameRate,
			dimensions, filter, scale, animationFile.frameRate as int,
			animationFile instanceof Streaming ? animationFile.streamingDataWorker : null)

		executorService.execute(animationDataWorker)
	}

	/**
	 * Constructor, use all of the given data for building an animation.
	 * 
	 * @param width
	 * @param height
	 * @param format
	 * @param numFrames
	 * @param frameRate
	 * @param dimensions
	 * @param filter
	 * @param scale
	 * @param bufferSize
	 * @param animationDataWorker
	 */
	@PackageScope
	Animation(int width, int height, int format, int numFrames, float frameRate, Rectanglef dimensions, boolean filter,
		boolean scale, int bufferSize = 10, Worker animationDataWorker) {

		if (!animationDataWorker) {
			throw new UnsupportedOperationException('Streaming configuration used, but source doesn\'t support streaming')
		}

		this.width      = width << (scale ? 1 : 0)
		this.height     = height << (scale ? 1 : 0)
		this.format     = format
		this.numFrames  = numFrames
		this.frameRate  = frameRate
		this.dimensions = dimensions
		this.filter     = filter

		frames = new ArrayBlockingQueue<>(bufferSize)
		this.bufferSize = bufferSize
		this.animationDataWorker = animationDataWorker
		this.animationDataWorker.on(StreamingFrameEvent) { event ->
			def frame = event.frame
			if (scale) {
				frame = event.frame.scale(width, height, format, 1)
			}
			frames << ByteBuffer.fromBuffersDirect(frame)
			if (bufferReady.count && !frames.remainingCapacity()) {
				bufferReady.countDown()
			}
		}
	}

	@Override
	void delete(GraphicsRenderer renderer) {

		animationDataWorker.stop()
		frames.drain()
		renderer.deleteTextures(textureIds.findAll { it } as int[])
	}

	@Override
	void init(GraphicsRenderer renderer) {

		lastFrame = -1
		textureIds = []
		framesQueued = 0
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

			// Try to load up to bufferSize frames ahead, maxing at 5 so we don't spent too much time in here
			if (frames.size()) {
				def framesAhead = Math.min(currentFrame + bufferSize, numFrames)
				if (!textureIds[framesAhead]) {
					def numFramesToRead = framesAhead - framesQueued
					if (numFramesToRead) {
						def newTextureIds = frames.drain(Math.max(numFramesToRead, 5)).collect { frame ->
							def newTextureId = renderer.createTexture(frame, format, width, height, filter)
							textureIds << newTextureId
							return newTextureId
						}
						framesQueued += newTextureIds.size()
					}
				}
			}

			// Draw the current frame if available
			if (currentFrame < numFrames) {
				def textureId = textureIds[currentFrame]
				if (textureId) {
					renderer.drawTexture(textureId, dimensions)
				}
				else {
					logger.debug('Frame {} not available, skipping', currentFrame)
				}
			}

			// Animation over
			else {
				stop()
			}

			// Delete used frames as the animation progresses to free up memory
			if (lastFrame != -1 && lastFrame != currentFrame) {
				def usedTextureIds = textureIds[lastFrame..<currentFrame]
				renderer.deleteTextures(usedTextureIds as int[])
				usedTextureIds.clear()
				framesQueued -= usedTextureIds.size()
			}
			lastFrame = currentFrame
		}
	}
}
