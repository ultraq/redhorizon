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

import nz.net.ultraq.redhorizon.audio.Music

import org.joml.Matrix4fc
import org.joml.Vector3fc
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

/**
 * Image and sound data from any supported {@link VideoDecoder}, with methods to
 * play them back.
 *
 * <p>Input streams will be decoded in a separate thread and loaded over time.
 * Whichever thread is used for updating audio or video will need to call their
 * respective {@code update*} methods periodically to keep the video fed.
 *
 * @author Emanuel Rabina
 */
class Video implements AutoCloseable {

	private static final Logger logger = LoggerFactory.getLogger(Video)

	private final Animation animation
	private final Music music
	private final ExecutorService executor = Executors.newSingleThreadExecutor()
	private Future<?> decodingTask
	private volatile boolean animationReady
	private volatile boolean musicReady
	private boolean decodingError

	/**
	 * Constructor, set up a new video from its name and a stream of data.
	 */
	@SuppressWarnings('UnnecessaryQualifiedReference')
	Video(String fileName, InputStream inputStream) {

		var decoder = VideoDecoders.forFileExtension(fileName.substring(fileName.lastIndexOf('.') + 1))

		animation = new Animation(decoder, 31)
			.on(Animation.PlaybackReadyEvent) { event ->
				animationReady = true
			}
		music = new Music(decoder, 32)
			.on(Music.PlaybackReadyEvent) { event ->
				musicReady = true
			}

		decodingTask = executor.submit { ->
			Thread.currentThread().name = "Video ${fileName} :: Decoding"
			try {
				logger.debug('Video decoding of {} started', fileName)
				var result = decoder.decode(inputStream)
				logger.debug('{} decoded after {} frames and {} samples', fileName, result.frames(), result.buffers())
				var fileInformation = result.fileInformation()
				if (fileInformation) {
					logger.info('{}: {}', fileName, fileInformation)
				}
			}
			catch (Exception ex) {
				logger.error('Error decoding video', ex)
				decodingError = true
			}
		}

		// Let each of the animation and music streams fill up first
		while (!(animationReady && musicReady)) {
			Thread.onSpinWait()
		}
		update(0f)
	}

	@Override
	void close() {

		executor.close()
		music.close()
		animation.close()
	}

	/**
	 * Return whether the video is currently playing.
	 */
	boolean isPlaying() {

		return animation.playing
	}

	/**
	 * Play the video.
	 */
	Video play() {

		animation.play()
		music.play()
		return this
	}

	/**
	 * Draw the current frame of the video.
	 */
	void render(SceneShaderContext shaderContext, Matrix4fc transform) {

		animation.render(shaderContext, transform)
	}

	/**
	 * Continue playback of the audio stream for this video.
	 */
	void render(Vector3fc position) {

		music.render(position)
	}

	/**
	 * Stop the video.
	 */
	Video stop() {

		decodingTask.cancel(true)
		animation.stop()
		music.stop()
		return this
	}

	/**
	 * Update the streaming data for the video.
	 */
	void update(float delta) {

		animation.update(delta)
		music.update()
	}
}
