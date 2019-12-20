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
	private final Worker frameDataWorker
	private final BlockingQueue<ByteBuffer> frameDataBuffer
	private long animationTimeStart

	// Rendering information
	private Rectanglef textureRect
	private List<FrameInfo> framesInfo

	/**
	 * Information about the rendering state of a frame.
	 */
	private class FrameInfo {
		int textureId
		ByteBuffer frame
	}

	/**
	 * Constructor, create an animation out of animation file data.
	 * 
	 * @param animationFile
	 * @param executorService
	 */
	Animation(AnimationFile animationFile, ExecutorService executorService) {

		width     = animationFile.width
		height    = animationFile.height
		format    = animationFile.format.value
		numFrames = animationFile.numFrames
		frameRate = animationFile.frameRate

		frameDataBuffer = new ArrayBlockingQueue<>(Math.ceil(animationFile.frameRate) as int)
		// TODO: Some kind of cached buffer so that some items don't need to be decoded again
		frameDataWorker = animationFile.getFrameDataWorker { frameData ->
			frameDataBuffer << frameData
		}
		executorService.execute(frameDataWorker)

		textureRect = new Rectanglef(-width / 2, -height / 2, width / 2, height / 2)
	}

	@Override
	void delete(GraphicsRenderer renderer) {

		frameDataWorker.stop()
		frameDataBuffer.drain()
		renderer.deleteTextures(framesInfo.textureId as int[])
	}

	@Override
	void init(GraphicsRenderer renderer) {

		framesInfo = new ArrayList<>(numFrames)
		loadFrames()
	}

	/**
	 * Transfer any decoded frames to the frame info buffer in an effort to get
	 * ahead of the current frame to play.
	 */
	private void loadFrames() {

		frameDataBuffer.drain().each { frame ->
			framesInfo << new FrameInfo(frame: ImageUtility.flipVertically(frame, width, height, format))
		}
	}

	@Override
	void play() {

		animationTimeStart = System.currentTimeMillis()
		Playable.super.play()
	}

	@Override
	void render(GraphicsRenderer renderer) {

		if (playing) {
			loadFrames()

			// Draw the current frame
			def currentFrameIndex = Math.floor((System.currentTimeMillis() - animationTimeStart) / 1000 * frameRate) as int
			if (currentFrameIndex < numFrames) {
				def currentFrameInfo = framesInfo[currentFrameIndex]
				if (!currentFrameInfo.textureId) {
					currentFrameInfo.textureId = renderer.createTexture(currentFrameInfo.frame, format, width, height)
				}
				renderer.drawTexture(currentFrameInfo.textureId, textureRect)

				// TODO: Clear frames/textures as the animation progresses to reduce memory usage
			}
			else {
				stop()
			}
		}
	}
}
