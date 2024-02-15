/*
 * Copyright 2024, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine.scenegraph.nodes

import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.ShaderRequest
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.SpriteMeshRequest
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.TextureRequest
import nz.net.ultraq.redhorizon.engine.graphics.Material
import nz.net.ultraq.redhorizon.engine.graphics.Mesh
import nz.net.ultraq.redhorizon.engine.graphics.Shader
import nz.net.ultraq.redhorizon.engine.graphics.Texture
import nz.net.ultraq.redhorizon.engine.graphics.opengl.SpriteShader
import nz.net.ultraq.redhorizon.engine.scenegraph.GraphicsElement
import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.engine.scenegraph.Playable
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.engine.scenegraph.Temporal
import nz.net.ultraq.redhorizon.filetypes.AnimationFile
import nz.net.ultraq.redhorizon.filetypes.Streaming
import nz.net.ultraq.redhorizon.filetypes.StreamingDecoder
import nz.net.ultraq.redhorizon.filetypes.StreamingFrameEvent

import java.util.concurrent.Executors

/**
 * An animation to play from a series of images/frames.
 *
 * @author Emanuel Rabina
 */
class Animation extends Node<Animation> implements GraphicsElement, Playable, Temporal {

	AnimationFile animationFile
	StreamingDecoder streamingDecoder

	// TODO: Maybe remove the need for these copies of animation properties if we
	//       have the streaming decoder carry them as well, ie: make both file and
	//       decoder an `AnimationSource` with those properties 🤔
	private final float frameRate
	private final int numFrames

	private final List<Texture> frames = []
	private long startTimeMs
	private int currentFrame = -1
	private int lastFrame = -1
	private Mesh mesh
	private Shader shader
	private Material material

	Animation(AnimationFile animationFile) {

		this(animationFile.width, animationFile.height, animationFile.forVgaMonitors, animationFile.frameRate,
			animationFile.numFrames, animationFile instanceof Streaming ? animationFile.streamingDecoder : null)

		this.animationFile = animationFile
	}

	Animation(int width, int height, boolean forVgaMonitors, float frameRate, int numFrames, StreamingDecoder streamingDecoder) {

		bounds
			.set(0, 0, width, forVgaMonitors ? height * 1.2f as float : height)
			.center()

		this.frameRate = frameRate
		this.numFrames = numFrames
		this.streamingDecoder = streamingDecoder
	}

	@Override
	void onSceneAdded(Scene scene) {

		if (!animationFile && !streamingDecoder) {
			throw new IllegalStateException('Cannot add an Animation node to a scene without a streaming or file source')
		}

		mesh = scene
			.requestCreateOrGet(new SpriteMeshRequest(bounds))
			.get()
		shader = scene
			.requestCreateOrGet(new ShaderRequest(SpriteShader.NAME))
			.get()
		material = new Material()

		if (streamingDecoder) {
			var buffersAdded = 0
			streamingDecoder.on(StreamingFrameEvent) { event ->
				frames << scene
					.requestCreateOrGet(new TextureRequest(event.width, event.height, event.format, event.frameFlippedVertical))
					.get()
				buffersAdded++
				if (buffersAdded == Math.ceil(frameRate) as int) {
					trigger(new PlaybackReadyEvent())
				}
			}

			// Run ourselves, otherwise expect the source to run this
			if (animationFile) {
				Executors.newVirtualThreadPerTaskExecutor().execute(streamingDecoder)
			}
			else {
				trigger(new StreamingReadyEvent())
			}
		}
		else {
			Executors.newVirtualThreadPerTaskExecutor().execute { ->
				var width = animationFile.width
				var height = animationFile.height
				var format = animationFile.format
				animationFile.frameData.each { frame ->
					frames << scene
						.requestCreateOrGet(new TextureRequest(width, height, format, frame.flipVertical(width, height, format)))
						.get()
				}
			}
		}
	}

	@Override
	void onSceneRemoved(Scene scene) {

		streamingDecoder?.cancel(true)
		scene.requestDelete(mesh, *(frames.findAll { frame -> frame }))
	}

	@Override
	void render(GraphicsRenderer renderer) {

		if (mesh && shader && material && currentFrame != -1) {

			// Draw the current frame if available
			var currentFrameTexture = frames[currentFrame]
			if (currentFrameTexture) {
				material.texture = currentFrameTexture
				var globalTransform = getGlobalTransform()
				renderer.draw(mesh, globalTransform, shader, material)
			}

			// Delete used frames as the animation progresses to free up memory
			if (streamingDecoder) {
				if (currentFrame > 0) {
					for (var i = lastFrame; i < currentFrame; i++) {
						renderer.deleteTexture(frames[i])
						frames[i] = null
					}
				}
			}

			lastFrame = currentFrame
		}
	}

	@Override
	void tick(long updatedTimeMs) {

		if (playing) {
			Temporal.super.tick(updatedTimeMs)

			if (!startTimeMs) {
				startTimeMs = currentTimeMs
			}

			var nextFrame = Math.floor((currentTimeMs - startTimeMs) / 1000 * frameRate) as int
			if (nextFrame < numFrames) {
				currentFrame = nextFrame
			}
			else {
				stop()
			}
		}
	}
}
