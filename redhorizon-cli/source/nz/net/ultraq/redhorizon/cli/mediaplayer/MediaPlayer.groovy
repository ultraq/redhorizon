/* 
 * Copyright 2022, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.cli.mediaplayer

import nz.net.ultraq.redhorizon.classic.PaletteType
import nz.net.ultraq.redhorizon.classic.filetypes.PalFile
import nz.net.ultraq.redhorizon.engine.Application
import nz.net.ultraq.redhorizon.engine.EngineLoopStartEvent
import nz.net.ultraq.redhorizon.engine.audio.AudioConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.engine.input.KeyEvent
import nz.net.ultraq.redhorizon.engine.media.AnimationLoader
import nz.net.ultraq.redhorizon.engine.media.ImageLoader
import nz.net.ultraq.redhorizon.engine.media.ImagesLoader
import nz.net.ultraq.redhorizon.engine.media.MediaLoader
import nz.net.ultraq.redhorizon.engine.media.SoundLoader
import nz.net.ultraq.redhorizon.engine.media.StopEvent
import nz.net.ultraq.redhorizon.engine.media.VideoLoader
import nz.net.ultraq.redhorizon.filetypes.AnimationFile
import nz.net.ultraq.redhorizon.filetypes.ImageFile
import nz.net.ultraq.redhorizon.filetypes.ImagesFile
import nz.net.ultraq.redhorizon.filetypes.SoundFile
import nz.net.ultraq.redhorizon.filetypes.VideoFile

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE
import static org.lwjgl.glfw.GLFW.GLFW_PRESS

/**
 * Media player application for watching media files.
 * 
 * @author Emanuel Rabina
 */
class MediaPlayer extends Application {

	// List of hi-res PCX images used: aftr_hi, alipaper, aly1, apc_hi, aphi0049,
	// bnhi0020, dchi0040, frhi0166, lab, landsbrg, mahi0107, mig_hi, mtfacthi,
	// needle, sov2, spy, stalin, tent.

	// List of soundtrack files available: intro, map, await, bigf226m, crus226m,
	// dense_r, fac1226m, fac2226m, fogger1a, hell226m, mud1a, radio2, rollout,
	// run1226m, smsh226m, snake, tren226m, terminat, twin, vector1a, work226m.

	private static final Logger logger = LoggerFactory.getLogger(MediaPlayer)

	private final Object mediaFile
	private final PaletteType paletteType
	private MediaLoader mediaLoader

	/**
	 * Constructor, create a new application around the given media file.
	 * 
	 * @param mediaFile
	 * @param audioConfig
	 * @param graphicsConfig
	 * @param paletteType
	 */
	MediaPlayer(Object mediaFile, AudioConfiguration audioConfig, GraphicsConfiguration graphicsConfig, PaletteType paletteType) {

		super('Media Player', audioConfig, graphicsConfig)
		this.mediaFile = mediaFile
		this.paletteType = paletteType
	}

	@Override
	protected void applicationStart() {

		logger.info('File details: {}', mediaFile)

		mediaLoader = switch (mediaFile) {
			case VideoFile -> loadVideo(mediaFile)
			case AnimationFile -> loadAnimation(mediaFile)
			case SoundFile -> loadSound(mediaFile)
			case ImageFile -> loadImage(mediaFile)
			case ImagesFile -> loadImages(mediaFile)
			default -> throw new UnsupportedOperationException("No media player for the associated file class of ${mediaFile}")
		}

		// Universal quit on exit
		inputEventStream.on(KeyEvent) { event ->
			if (event.action == GLFW_PRESS && event.key == GLFW_KEY_ESCAPE) {
				stop()
			}
		}
	}

	@Override
	protected void applicationStop() {

		mediaLoader.unload()
	}

	/**
	 * Load up an animation for playing.
	 * 
	 * @param animationFile
	 */
	private AnimationLoader loadAnimation(AnimationFile animationFile) {

		def animationLoader = new AnimationLoader(animationFile, scene, graphicsEngine, gameClock, inputEventStream)

		graphicsEngine.on(EngineLoopStartEvent) { engineLoopStartEvent ->
			def animation = animationLoader.load()
			animation.play()
			logger.debug('Animation started')

			animation.on(StopEvent) { stopEvent ->
				stop()
				logger.debug('Animation stopped')
			}

			logger.info('Waiting for animation to finish.  Close the window to exit.')
		}

		return animationLoader
	}

	/**
	 * Load an image for viewing.
	 * 
	 * @param imageFile
	 */
	private ImageLoader loadImage(ImageFile imageFile) {

		def imageLoader = new ImageLoader(imageFile, scene, graphicsEngine)

		graphicsEngine.on(EngineLoopStartEvent) { event ->
			imageLoader.load()
			logger.info('Displaying the image.  Close the window to exit.')
		}

		return imageLoader
	}

	/**
	 * Load a collection of images for viewing.
	 * 
	 * @param imagesFile
	 */
	private ImagesLoader loadImages(ImagesFile imagesFile) {

		return getResourceAsStream(paletteType.file).withBufferedStream { inputStream ->
			def palette = new PalFile(inputStream)
			def imagesLoader = new ImagesLoader(imagesFile, palette, scene, graphicsEngine, inputEventStream)

			graphicsEngine.on(EngineLoopStartEvent) { event ->
				imagesLoader.load()
				logger.info('Displaying the image.  Close the window to exit.')
			}

			return imagesLoader
		}
	}

	/**
	 * Load a sound effect or music track for playing.
	 * 
	 * @param soundFile
	 */
	private SoundLoader loadSound(SoundFile soundFile) {

		def soundLoader = new SoundLoader(soundFile, scene, gameClock, inputEventStream)

		audioEngine.on(EngineLoopStartEvent) { event ->
			def sound = soundLoader.load()
			sound.play()
			logger.debug('Sound started')

			sound.on(StopEvent) { stopEvent ->
				stop()
				logger.debug('Sound stopped')
			}

			logger.info('Waiting for sound to stop playing.  Close the window to exit.')
		}

		return soundLoader
	}

	/**
	 * Load a video for playing.
	 * 
	 * @param videoFile
	 */
	private VideoLoader loadVideo(VideoFile videoFile) {

		def videoLoader = new VideoLoader(videoFile, scene, graphicsEngine, gameClock, inputEventStream)

		graphicsEngine.on(EngineLoopStartEvent) { event ->
			def video = videoLoader.load()
			video.play()
			logger.debug('Video started')

			video.on(StopEvent) { stopEvent ->
				stop()
				logger.debug('Video stopped')
			}

			logger.info('Waiting for video to finish.  Close the window to exit.')
		}

		return videoLoader
	}
}
