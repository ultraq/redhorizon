/*
 * Copyright 2025, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.graphics

import nz.net.ultraq.redhorizon.graphics.ImageDecoder.FrameDecodedEvent
import nz.net.ultraq.redhorizon.graphics.ImageDecoder.HeaderDecodedEvent
import nz.net.ultraq.redhorizon.graphics.Mesh.Type
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLMesh
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLTexture
import nz.net.ultraq.redhorizon.scenegraph.Node

import org.joml.Vector2f
import org.joml.Vector3f
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * A series of frames taken from any {@link ImageDecoder}-supported type, with
 * methods to play them back as an animation.
 *
 * <p>Input streams will be decoded in a separate thread and loaded over time.
 * Whichever thread is used for updating audio will need to call {@link #update}
 * periodically to keep the animation fed.
 *
 * @author Emanuel Rabina
 */
class Animation extends Node<Animation> implements AutoCloseable {

	private static final Logger logger = LoggerFactory.getLogger(Animation)
	private static final int[] index = new int[]{ 0, 1, 2, 2, 3, 0 }

	private int width
	private int height
	private int numFrames
	private float frameRate
	private boolean playing
	private float playTimer
	private int currentFrame
	private int lastFrame
	private final ExecutorService executor = Executors.newSingleThreadExecutor()
	private volatile BlockingQueue<FrameDecodedEvent> streamingEvents
	private final List<FrameDecodedEvent> eventDrain = []
	private boolean decodingError
	private Mesh mesh
	private Material material = new Material()
	private final List<Texture> frames = []

	/**
	 * Constructor, set up a new animation from its name and a stream of data.
	 */
	Animation(String fileName, InputStream inputStream, int playbackWidth = 0, int playbackHeight = 0) {

		var decoder = ImageDecoders.forFileExtension(fileName.substring(fileName.lastIndexOf('.') + 1))
			.on(HeaderDecodedEvent) { event ->
				width = playbackWidth ?: event.width()
				height = playbackHeight ?: event.height()
				numFrames = event.numFrames()
				frameRate = event.frameRate()
				streamingEvents = new ArrayBlockingQueue<>(frameRate as int)
			}
			.on(FrameDecodedEvent) { event ->
				streamingEvents << event
			}
		executor.submit { ->
			Thread.currentThread().name = "Animation ${fileName} :: Decoding"
			try {
				logger.debug('Animation decoding of {} started', fileName)
				var result = decoder.decode(inputStream)
				logger.debug('{} decoded after {} frames', fileName, result.frames())
				var fileInformation = result.fileInformation()
				if (fileInformation) {
					logger.info('{}: {}', fileName, fileInformation)
				}
			}
			catch (Exception ex) {
				logger.error('Error decoding animation', ex)
				decodingError = true
			}
		}

		// Let the decode buffer fill up first
		while (!streamingEvents || streamingEvents.remainingCapacity()) {
			Thread.onSpinWait()
		}

		// Mesh for each of the frames to render into
		mesh = new OpenGLMesh(Type.TRIANGLES, new Vertex[]{
			new Vertex(new Vector3f(0, 0, 0), Colour.WHITE, new Vector2f(0, 0)),
			new Vertex(new Vector3f(width, 0, 0), Colour.WHITE, new Vector2f(1, 0)),
			new Vertex(new Vector3f(width, height, 0), Colour.WHITE, new Vector2f(1, 1)),
			new Vertex(new Vector3f(0, height, 0), Colour.WHITE, new Vector2f(0, 1))
		}, index)

		update(0f)
	}

	@Override
	void close() {

		executor.close()
		frames.eachWithIndex { frame, index ->
			if (frame) {
				frame.close()
			}
		}
	}

	/**
	 * Draw the current frame of the animation.
	 */
	void draw(SceneShaderContext shaderContext) {

		material.texture = frames[currentFrame]
		mesh.draw(shaderContext, material, transform)
	}

	/**
	 * Return whether the animation is currently playing.
	 */
	boolean isPlaying() {

		return playing
	}

	/**
	 * Play the animation.
	 */
	Animation play() {

		playing = true
		playTimer = 0
		return this
	}

	/**
	 * Stop the animation.
	 */
	Animation stop() {

		playing = false
		return this
	}

	/**
	 * Update the streaming data for the animation.
	 */
	void update(float delta) {

		if (decodingError) {
			throw new IllegalStateException('An error occurred decoding the animation')
		}

		// Buffer the animation
		var framesAhead = Math.min(currentFrame + frameRate, numFrames - 1) as int
		if (!frames[framesAhead]) {
			eventDrain.clear()
			streamingEvents.drain(eventDrain, framesAhead - currentFrame).each { event ->
				frames << new OpenGLTexture(event.width(), event.height(), event.channels(),
					event.data().flipVertical(event.width(), event.height(), event.channels()))
			}
		}

		// Advance the current frame (if playing)
		if (playing) {
			playTimer += delta
			var nextFrame = playTimer * frameRate as int
			if (nextFrame < numFrames) {
				currentFrame = nextFrame
			}
			else {
				stop()
			}
		}

		// Close any used frames
		var framesProcessed = currentFrame - lastFrame
		if (framesProcessed) {
			framesProcessed.times { i ->
				var index = lastFrame + i
				frames[index].close()
				frames[index] = null
			}
			lastFrame = currentFrame
		}
	}
}
