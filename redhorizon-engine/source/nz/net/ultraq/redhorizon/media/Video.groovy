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
import nz.net.ultraq.redhorizon.engine.audio.AudioElement
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsElement
import nz.net.ultraq.redhorizon.filetypes.Streaming
import nz.net.ultraq.redhorizon.filetypes.VideoFile
import nz.net.ultraq.redhorizon.scenegraph.SceneElement
import nz.net.ultraq.redhorizon.scenegraph.SceneVisitor

import org.joml.Matrix4f
import org.joml.Rectanglef
import org.joml.Vector3f

import java.util.concurrent.ExecutorService

/**
 * The combination of an animation and sound track, a video is a stream from a
 * video file, rendering out to both the audio and graphics engines during
 * playback.
 * 
 * @author Emanuel Rabina
 */
class Video implements AudioElement, GraphicsElement, Playable, SceneElement {

	@Delegate
	private final Animation animation
	@Delegate
	private final SoundTrack soundTrack

	/**
	 * Constructor, creates a video out of video file data.
	 * 
	 * @param videoFile
	 *   Video source.
	 * @param scale
	 *   Whether or not to double the input resolution of low-resolution videos.
	 * @param gameTime
	 * @param executorService
	 */
	Video(VideoFile videoFile, boolean scale, GameTime gameTime, ExecutorService executorService) {

		if (videoFile instanceof Streaming) {
			def videoWorker = videoFile.streamingDataWorker

			animation = new Animation(videoFile.width, videoFile.height, videoFile.format, videoFile.numFrames, videoFile.frameRate,
				scale, videoFile.frameRate * 2 as int, videoWorker, gameTime)
			animation.on(StopEvent) { event ->
				stop()
			}

			soundTrack = new SoundTrack(videoFile.bits, videoFile.channels, videoFile.frequency,
				videoFile.frameRate * 2 + 1 as int, videoWorker, gameTime)
			soundTrack.on(StopEvent) { event ->
				stop()
			}

			executorService.execute(videoWorker)
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
	Rectanglef getBounds() {

		return SceneElement.super.bounds
	}

	@Override
	Matrix4f getTransform() {

		return SceneElement.super.transform
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
	SceneElement scaleXY(float factor) {

		[animation, soundTrack]*.scaleXY(factor)
		return SceneElement.super.scaleXY(factor)
	}

	@Override
	void stop() {

		[animation, soundTrack]*.stop()
		Playable.super.stop()
	}

	@Override
	SceneElement translate(Vector3f offset) {

		[animation, soundTrack]*.translate(offset)
		return SceneElement.super.translate(offset)
	}
}
