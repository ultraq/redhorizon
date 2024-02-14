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

import nz.net.ultraq.redhorizon.engine.audio.AudioRenderer
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.scenegraph.AudioElement
import nz.net.ultraq.redhorizon.engine.scenegraph.GraphicsElement
import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.engine.scenegraph.Playable
import nz.net.ultraq.redhorizon.engine.scenegraph.Scene
import nz.net.ultraq.redhorizon.filetypes.StreamingDecoder
import nz.net.ultraq.redhorizon.filetypes.VideoFile

import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

/**
 * The combination of an animation and sound track, a video is a stream from a
 * video file, rendering out to both the audio and graphics engines during
 * playback.
 *
 * @author Emanuel Rabina
 */
class Video extends Node<Video> implements AudioElement, GraphicsElement, Playable {

	final VideoFile videoFile

	private final StreamingDecoder streamingDecoder
	private final Animation animation
	private final Sound sound

	Video(VideoFile videoFile) {

		streamingDecoder = videoFile.streamingDecoder

		animation = new Animation(videoFile.width, videoFile.height, videoFile.forVgaMonitors, videoFile.frameRate,
			videoFile.numFrames, streamingDecoder)
		bounds.set(animation.bounds)
		addChild(animation)

		sound = new Sound(streamingDecoder)
		addChild(sound)

		this.videoFile = videoFile
	}

	@Override
	void onSceneAdded(Scene scene) {

		var executor = Executors.newVirtualThreadPerTaskExecutor()
		var startExecutorLatch = new CountDownLatch(2)
		[animation, sound]*.on(StreamingReadyEvent) { event ->
			startExecutorLatch.countDown()
		}
		executor.execute { ->
			startExecutorLatch.await()
			executor.execute(streamingDecoder)
		}

		var buffersReadyLatch = new CountDownLatch(2)
		[animation, sound]*.on(PlaybackReadyEvent) { event ->
			buffersReadyLatch.countDown()
		}
		executor.execute { ->
			buffersReadyLatch.await()
			trigger(new PlaybackReadyEvent())
		}
	}

	@Override
	void pause() {

		[animation, sound]*.pause()
		Playable.super.pause()
	}

	@Override
	void play() {

		[animation, sound]*.play()
		Playable.super.play()
	}

	@Override
	void render(AudioRenderer renderer) {
	}

	@Override
	void render(GraphicsRenderer renderer) {
	}

	@Override
	void stop() {

		[animation, sound]*.stop()
		Playable.super.stop()
	}
}
