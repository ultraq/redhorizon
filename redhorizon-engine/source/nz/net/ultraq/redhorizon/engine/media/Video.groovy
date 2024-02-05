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

package nz.net.ultraq.redhorizon.engine.media

import nz.net.ultraq.redhorizon.engine.audio.AudioElement
import nz.net.ultraq.redhorizon.engine.audio.AudioRenderer
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsElement
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsRenderer
import nz.net.ultraq.redhorizon.engine.scenegraph.Node
import nz.net.ultraq.redhorizon.engine.scenegraph.SceneVisitor
import nz.net.ultraq.redhorizon.filetypes.Streaming
import nz.net.ultraq.redhorizon.filetypes.VideoFile

import java.util.concurrent.Executors

/**
 * The combination of an animation and sound track, a video is a stream from a
 * video file, rendering out to both the audio and graphics engines during
 * playback.
 *
 * @author Emanuel Rabina
 */
class Video extends Node<Video> implements AudioElement, GraphicsElement, Playable {

	private final Animation animation
	private final SoundTrack soundTrack

	/**
	 * Constructor, creates a video out of video file data.
	 *
	 * @param videoFile Video source.
	 */
	Video(VideoFile videoFile) {

		if (videoFile instanceof Streaming) {
			def videoWorker = videoFile.streamingDecoder

			animation = new Animation(videoFile.width, videoFile.height, videoFile.format, videoFile.numFrames, videoFile.frameRate,
				videoFile.frameRate * 2 as int, videoWorker)
			animation.on(StopEvent) { event ->
				stop()
			}

			soundTrack = new SoundTrack(videoFile.bits, videoFile.channels, videoFile.frequency,
				videoFile.frameRate * 2 + 1 as int, videoWorker)
			soundTrack.on(StopEvent) { event ->
				stop()
			}

			Executors.newSingleThreadExecutor().execute(videoWorker)
		}
		else {
			throw new UnsupportedOperationException('Streaming configuration used, but source doesn\'t support streaming')
		}
	}

	@Override
	void accept(SceneVisitor visitor) {

		visitor.visit(animation)
		visitor.visit(soundTrack)
	}

	@Override
	void delete(AudioRenderer renderer) {

	}

	@Override
	void delete(GraphicsRenderer renderer) {

	}

	@Override
	void init(AudioRenderer renderer) {

	}

	@Override
	void init(GraphicsRenderer renderer) {

	}

	@Override
	boolean isPlaying() {

		return animation.playing && soundTrack.playing
	}

	@Override
	void play() {

		[animation, soundTrack]*.play()
		Playable.super.play()
	}

	@Override
	void render(AudioRenderer renderer) {

	}

	@Override
	void render(GraphicsRenderer renderer) {

	}

	@Override
	Video scale(float x, float y, float z) {

		[animation, soundTrack]*.scale(x, y, z)
		return this
	}

	@Override
	void stop() {

		[animation, soundTrack]*.stop()
	}

	@Override
	Video translate(float x, float y, float z) {

		[animation, soundTrack]*.translate(x, y, z)
		return this
	}
}
