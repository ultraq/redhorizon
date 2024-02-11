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

import nz.net.ultraq.redhorizon.engine.graphics.GraphicsElement
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.ShaderRequest
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.SpriteMeshRequest
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRequests.TextureRequest
import nz.net.ultraq.redhorizon.engine.graphics.Material
import nz.net.ultraq.redhorizon.engine.graphics.Mesh
import nz.net.ultraq.redhorizon.engine.graphics.Shader
import nz.net.ultraq.redhorizon.engine.graphics.Texture
import nz.net.ultraq.redhorizon.engine.graphics.opengl.SpriteShader
import nz.net.ultraq.redhorizon.engine.media.Playable
import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.engine.time.Temporal
import nz.net.ultraq.redhorizon.filetypes.AnimationFile
import nz.net.ultraq.redhorizon.filetypes.Streaming
import nz.net.ultraq.redhorizon.filetypes.StreamingFrameEvent

import java.nio.ByteBuffer
import java.util.concurrent.Executors

/**
 * An animation to play from a series of images/frames.
 *
 * @author Emanuel Rabina
 */
class Animation extends Node<Animation> implements GraphicsElement, Playable, Temporal {

	final AnimationFile animationFile

	private final List<Texture> frames = []
	private long startTimeMs
	private int currentFrame = -1
	private int lastFrame = -1
	private Mesh mesh
	private Shader shader
	private Material material

	Animation(AnimationFile animationFile) {

		bounds
			.set(0, 0, animationFile.width, animationFile.forVgaMonitors ? animationFile.height * 1.2f as float : animationFile.height)
			.center()
		this.animationFile = animationFile
	}

	@Override
	void delete(GraphicsRenderer renderer) {
	}

	@Override
	void init(GraphicsRenderer renderer) {
	}

	@Override
	void onSceneAdded(Scene scene) {

		mesh = scene
			.requestCreateOrGet(new SpriteMeshRequest(bounds))
			.get()
		shader = scene
			.requestCreateOrGet(new ShaderRequest(SpriteShader.NAME))
			.get()
		material = new Material()

		var width = animationFile.width
		var height = animationFile.height
		var format = animationFile.format

		var requestFrame = { ByteBuffer frameData ->
			return scene
				.requestCreateOrGet(new TextureRequest(width, height, format, frameData.flipVertical(width, height, format)))
				.get()
		}

		if (animationFile instanceof Streaming) {
			var decoder = animationFile.streamingDecoder
			decoder.on(StreamingFrameEvent) { event ->
				frames << requestFrame(event.frame)
			}
			Executors.newVirtualThreadPerTaskExecutor().execute(decoder)
		}
		else {
			Executors.newVirtualThreadPerTaskExecutor().execute { ->
				animationFile.frameData.each { frame ->
					frames << requestFrame(frame)
				}
			}
		}
	}

	@Override
	void onSceneRemoved(Scene scene) {

		scene.requestDelete(mesh, shader, *frames)
	}

	@Override
	void render(GraphicsRenderer renderer) {

		if (mesh && shader && material && currentFrame != -1) {

			// Draw the current frame if available
			var currentFrameTexture = frames[currentFrame]
			if (currentFrameTexture) {
				material.texture = currentFrameTexture
				renderer.draw(mesh, getGlobalTransform(), shader, material)
			}

			// Delete used frames as the animation progresses to free up memory
			// NOTE: This basically ties this class to play-once streaming animations only 🤔
			if (currentFrame > 0) {
				for (var i = lastFrame; i < currentFrame; i++) {
					renderer.deleteTexture(frames[i])
					frames[i] = null
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

			var nextFrame = Math.floor((currentTimeMs - startTimeMs) / 1000 * animationFile.frameRate) as int
			if (nextFrame < animationFile.numFrames) {
				currentFrame = nextFrame
			}
			else {
				stop()
			}
		}
	}
}
