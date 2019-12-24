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
import nz.net.ultraq.redhorizon.utilities.ImageUtility

import org.joml.Rectanglef

import java.nio.ByteBuffer
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ExecutorService

/**
 * An animation that plays a series of images at a certain speed.
 * 
 * @author Emanuel Rabina
 */
class Animation implements GraphicsElement, Playable, SelfVisitable {

	// Animation file attributes
	final int width
	final int height
	final int format
	final int numFrames
	final float frameRate
	final Rectanglef dimensions
	private final Worker frameDataWorker
	private final BlockingQueue<ByteBuffer> frameDataBuffer
	private long animationTimeStart

	// Rendering information
	private final List<ByteBuffer> frames = [] // TODO: This still keeps frames around in memory 🤔
	private final List<Integer> textureIds = [].withDefault { null }
	private int lastFrame = -1

	/**
	 * Constructor, create an animation out of animation file data.
	 * 
	 * @param animationFile   Animation source.
	 * @param dimensions      Dimensions over which to display the animation over.
	 * @param executorService
	 */
	Animation(AnimationFile animationFile, Rectanglef dimensions, ExecutorService executorService) {

		width     = animationFile.width
		height    = animationFile.height
		format    = animationFile.format.value
		numFrames = animationFile.numFrames
		frameRate = animationFile.frameRate
		this.dimensions = dimensions

		if (animationFile instanceof Streaming) {
			frameDataBuffer = new ArrayBlockingQueue<>(frameRate as int)
			// TODO: Some kind of cached buffer so that some items don't need to be decoded again
			frameDataWorker = animationFile.getStreamingDataWorker { frame ->
				frameDataBuffer << ImageUtility.flipVertically(frame, width, height, format)
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
		frameDataBuffer.drain()
		renderer.deleteTextures(*textureIds)
	}

	@Override
	void init(GraphicsRenderer renderer) {
	}

	@Override
	void play() {

		animationTimeStart = System.currentTimeMillis()
		Playable.super.play()
	}

	@Override
	void render(GraphicsRenderer renderer) {

		if (playing) {

			// Frames to load
			if (!frameDataBuffer.empty) {
				frameDataBuffer.drain().each { frame ->
					frames << frame // TODO: 🤔
				}
			}

			def currentFrame = Math.floor((System.currentTimeMillis() - animationTimeStart) / 1000 * frameRate) as int

			// Draw the current frame
			if (currentFrame < numFrames) {
				def textureId = textureIds[currentFrame]
				if (!textureId) {
					textureId = renderer.createTexture(frames[currentFrame], format, width, height)
					textureIds[currentFrame] = textureId
				}
				renderer.drawTexture(textureId, dimensions)
			}

			// Animation over
			else {
				stop()
			}

			// Delete used frames as the animation progresses to free up memory
			if (lastFrame != -1 && lastFrame != currentFrame) {
				renderer.deleteTextures(textureIds[lastFrame])
				lastFrame..<currentFrame.each { i ->
					frames[i] = null
				}
			}
			lastFrame = currentFrame
		}
	}
}
