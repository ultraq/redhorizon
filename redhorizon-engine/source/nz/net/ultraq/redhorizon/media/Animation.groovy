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

import nz.net.ultraq.redhorizon.engine.GameTime
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsElement
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.Material
import nz.net.ultraq.redhorizon.engine.graphics.Mesh
import nz.net.ultraq.redhorizon.engine.graphics.ShaderType
import nz.net.ultraq.redhorizon.engine.graphics.Texture
import nz.net.ultraq.redhorizon.filetypes.AnimationFile
import nz.net.ultraq.redhorizon.filetypes.ColourFormat
import nz.net.ultraq.redhorizon.filetypes.Streaming
import nz.net.ultraq.redhorizon.filetypes.StreamingFrameEvent
import nz.net.ultraq.redhorizon.filetypes.Worker
import nz.net.ultraq.redhorizon.scenegraph.SceneElement

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
class Animation implements GraphicsElement, Playable, SceneElement<Animation> {

	private static final Logger logger = LoggerFactory.getLogger(Animation)

	// Animation file attributes
	final int width
	final int height
	final ColourFormat format
	final int numFrames
	final float frameRate

	private final Worker animationDataWorker
	private final BlockingQueue<ByteBuffer> frames
	private final int bufferSize
	private final CountDownLatch bufferReady = new CountDownLatch(1)
	private int framesQueued
	private long animationTimeStart
	private final GameTime gameTime

	// Rendering information
	private int lastFrame
	private Material material
	private Mesh mesh
	private List<Texture> textures

	/**
	 * Constructor, create an animation out of animation file data.
	 * 
	 * @param animationFile
	 *   Animation source.
	 * @param scale
	 *   Whether or not to double the input resolution of low-resolution
	 *   animations.
	 * @param gameTime
	 * @param executorService
	 */
	Animation(AnimationFile animationFile, boolean scale, GameTime gameTime, ExecutorService executorService) {

		this(animationFile.width, animationFile.height, animationFile.format, animationFile.numFrames, animationFile.frameRate,
			scale, animationFile.frameRate as int,
			animationFile instanceof Streaming ? animationFile.streamingDataWorker : null,
			gameTime)

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
	 * @param scale
	 *   Whether or not to double the input resolution of low-resolution
	 *   animations.
	 * @param bufferSize
	 * @param animationDataWorker
	 * @param gameTime
	 */
	@PackageScope
	Animation(int width, int height, ColourFormat format, int numFrames, float frameRate,
		boolean scale, int bufferSize = 10, Worker animationDataWorker, GameTime gameTime) {

		if (!animationDataWorker) {
			throw new UnsupportedOperationException('Streaming configuration used, but source doesn\'t support streaming')
		}

		this.width     = width << (scale ? 1 : 0)
		this.height    = height << (scale ? 1 : 0)
		this.format    = format
		this.numFrames = numFrames
		this.frameRate = frameRate

		frames = new ArrayBlockingQueue<>(bufferSize)
		this.bufferSize = bufferSize
		this.animationDataWorker = animationDataWorker
		this.animationDataWorker.on(StreamingFrameEvent) { event ->
			def frame = event.frame.flipVertical(width, height, format)
			frames << (scale ? frame.scale(width, height, format, 1) : frame)
			if (bufferReady.count && !frames.remainingCapacity()) {
				bufferReady.countDown()
			}
		}

		this.gameTime = gameTime
	}

	@Override
	void delete(GraphicsRenderer renderer) {

		animationDataWorker.stop()
		frames.drain()
		renderer.deleteMesh(mesh)
		textures.each { texture ->
			renderer.deleteTexture(texture)
		}
	}

	@Override
	void init(GraphicsRenderer renderer) {

		lastFrame = -1
		mesh = renderer.createSpriteMesh(new Rectanglef(0, 0, width, height))
		material = renderer.createMaterial(mesh, null, ShaderType.TEXTURE)
		textures = []
		framesQueued = 0
	}

	@Override
	void play() {

		// Wait until the frame buffer is filled before starting play
		bufferReady.await()
		animationTimeStart = gameTime.currentTimeMillis
		Playable.super.play()
	}

	@Override
	void render(GraphicsRenderer renderer) {

		if (playing) {
			def currentFrame = Math.floor((gameTime.currentTimeMillis - animationTimeStart) / 1000 * frameRate) as int

			// Try to load up to bufferSize frames ahead, maxing at 5 so we don't spent too much time in here
			if (frames.size()) {
				def framesAhead = Math.min(currentFrame + bufferSize, numFrames)
				if (!textures[framesAhead]) {
					def numFramesToRead = framesAhead - framesQueued
					if (numFramesToRead) {
						frames.drain(Math.max(numFramesToRead, 5)).each { frame ->
							textures << renderer.createTexture(frame, format.value, width, height)
							framesQueued++
						}
					}
				}
			}

			// Draw the current frame if available
			if (currentFrame < numFrames) {
				def texture = textures[currentFrame]
				if (texture) {
					material.texture = texture
					renderer.drawMaterial(material, transform)
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
				def usedTextures = textures[lastFrame..<currentFrame]
				usedTextures.each { texture ->
					renderer.deleteTexture(texture)
				}
				usedTextures.clear()
				framesQueued -= usedTextures.size()
			}
			lastFrame = currentFrame
		}
	}
}
