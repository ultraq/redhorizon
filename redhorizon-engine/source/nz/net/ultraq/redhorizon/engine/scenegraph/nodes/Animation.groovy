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
import nz.net.ultraq.redhorizon.engine.graphics.opengl.Shaders
import nz.net.ultraq.redhorizon.engine.scenegraph.GraphicsElement
import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.engine.scenegraph.Playable
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.engine.scenegraph.SceneEvents
import nz.net.ultraq.redhorizon.engine.scenegraph.Temporal
import nz.net.ultraq.redhorizon.events.Event
import nz.net.ultraq.redhorizon.events.EventTarget
import nz.net.ultraq.redhorizon.filetypes.AnimationFile
import nz.net.ultraq.redhorizon.filetypes.Streaming
import nz.net.ultraq.redhorizon.filetypes.StreamingDecoder
import nz.net.ultraq.redhorizon.filetypes.StreamingFrameEvent

import groovy.transform.TupleConstructor
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

/**
 * An animation to play from a series of images/frames.
 *
 * @author Emanuel Rabina
 */
class Animation extends Node<Animation> implements GraphicsElement, Playable, Temporal {

	private final AnimationSource animationSource

	private final int numFrames
	private final float frameRate
	private long startTimeMs
	private int currentFrame = -1
	private Mesh mesh
	private Shader shader
	private Material material

	/**
	 * Constructor, create an animation using data straight from an animation file.
	 */
	Animation(AnimationFile animationFile) {

		this(animationFile.width, animationFile.height, animationFile.numFrames, animationFile.frameRate,
			animationFile.forVgaMonitors, new StreamingAnimationSource(((Streaming)animationFile).streamingDecoder, true))
	}

	/**
	 * Constructor, create an animation using any implementation of the
	 * {@link AnimationSource} interface.
	 */
	Animation(int width, int height, int numFrames, float frameRate, boolean forVgaMonitors = false,
		AnimationSource animationSource) {

		bounds.set(0, 0, width, forVgaMonitors ? height * 1.2f as float : height)
		this.numFrames = numFrames
		this.frameRate = frameRate

		this.animationSource = animationSource
		animationSource.relay(Event, this)
	}

	@Override
	CompletableFuture<Void> onSceneAdded(Scene scene) {

		return CompletableFuture.allOf(
			scene
				.requestCreateOrGet(new SpriteMeshRequest(bounds))
				.thenAcceptAsync { newMesh ->
					mesh = newMesh
				},
			scene
				.requestCreateOrGet(new ShaderRequest(Shaders.spriteShader))
				.thenAcceptAsync { requestedShader ->
					shader = requestedShader
				}
		)
			.thenComposeAsync { _ ->
				material = new Material()

				return animationSource.onSceneAdded(scene)
			}
	}

	@Override
	CompletableFuture<Void> onSceneRemoved(Scene scene) {

		stop()
		return CompletableFuture.allOf(
			animationSource.onSceneRemoved(scene),
			scene.requestDelete(mesh)
		)
	}

	@Override
	void render(GraphicsRenderer renderer) {

		if (mesh && shader && material && currentFrame != -1) {
			var frame = animationSource.prepareFrame(renderer, currentFrame)
			if (frame) {
				material.texture = frame
				renderer.draw(mesh, globalTransform, shader, material)
			}
		}
	}

	@Override
	void tick(long updatedTimeMs) {

		if (playing) {
			Temporal.super.tick(updatedTimeMs)

			if (!startTimeMs) {
				startTimeMs = currentTimeMs
			}
		}
	}

	@Override
	void update() {

		var nextFrame = Math.floor((currentTimeMs - startTimeMs) / 1000 * frameRate) as int
		if (nextFrame < numFrames) {
			currentFrame = nextFrame
		}
		else {
			stop()
		}
	}

	/**
	 * Interface for any source from which frames of animation can be obtained.
	 */
	static interface AnimationSource extends EventTarget, SceneEvents {

		/**
		 * Called during {@code render}, return the texture to be used for rendering
		 * the given frame of animation.
		 */
		Texture prepareFrame(GraphicsRenderer renderer, int frameNumber)
	}

	/**
	 * An animation source using a streaming animation file.
	 */
	@TupleConstructor(defaults = false)
	static class StreamingAnimationSource implements AnimationSource {

		final StreamingDecoder streamingDecoder
		final boolean autoStream

		private List<Texture> frames = []
		private int lastFrame = 0

		@Override
		Texture prepareFrame(GraphicsRenderer renderer, int frameNumber) {

			// Delete used frames as the animation progresses to free up memory
			while (lastFrame < frameNumber) {
				renderer.delete(frames[lastFrame])
				frames[lastFrame] = null
				lastFrame++
			}

			return frames[frameNumber]
		}

		@Override
		CompletableFuture<Void> onSceneAdded(Scene scene) {

			return CompletableFuture.runAsync { ->
				var buffersAdded = 0
				streamingDecoder.on(StreamingFrameEvent) { event ->
					frames << scene
						.requestCreateOrGet(new TextureRequest(event.width, event.height, event.format, event.frameFlippedVertical))
						.get()
					buffersAdded++
					if (buffersAdded == 10) {
						trigger(new PlaybackReadyEvent())
					}
				}

				// Run ourselves, otherwise expect the owner of this source to run this
				if (autoStream) {
					Executors.newVirtualThreadPerTaskExecutor().execute(streamingDecoder)
				}
				else {
					trigger(new StreamingReadyEvent())
				}
			}
		}

		@Override
		CompletableFuture<Void> onSceneRemoved(Scene scene) {

			streamingDecoder.cancel(true)
			return scene.requestDelete(*frames)
		}
	}
}
