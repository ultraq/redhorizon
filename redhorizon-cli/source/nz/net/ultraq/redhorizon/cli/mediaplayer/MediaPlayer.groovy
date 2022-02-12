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

import nz.net.ultraq.redhorizon.Application
import nz.net.ultraq.redhorizon.classic.PaletteType
import nz.net.ultraq.redhorizon.engine.EngineLoopStartEvent
import nz.net.ultraq.redhorizon.engine.audio.AudioConfiguration
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.filetypes.AnimationFile
import nz.net.ultraq.redhorizon.filetypes.ImageFile
import nz.net.ultraq.redhorizon.filetypes.ImagesFile
import nz.net.ultraq.redhorizon.filetypes.SoundFile
import nz.net.ultraq.redhorizon.filetypes.VideoFile
import nz.net.ultraq.redhorizon.media.AnimationLoader
import nz.net.ultraq.redhorizon.media.StopEvent
import nz.net.ultraq.redhorizon.media.VideoLoader

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Media player application for watching media files.
 * 
 * @author Emanuel Rabina
 */
class MediaPlayer extends Application {

	private static final Logger logger = LoggerFactory.getLogger(MediaPlayer)

	private final Object mediaFile
	private final PaletteType paletteType

	/**
	 * Constructor, create a new application around the given media file.
	 * 
	 * @param mediaFile
	 * @param audioConfig
	 * @param graphicsConfig
	 * @param paletteType
	 */
	MediaPlayer(Object mediaFile, AudioConfiguration audioConfig, GraphicsConfiguration graphicsConfig, PaletteType paletteType) {

		super(null, audioConfig, graphicsConfig)
		this.mediaFile = mediaFile
		this.paletteType = paletteType
	}

	/**
	 * Load up an animation for playing.
	 * 
	 * @param animationFile
	 */
	private void loadAnimation(AnimationFile animationFile) {

		def animationLoader = new AnimationLoader(scene, graphicsEngine, inputEventStream, gameClock)
		animationLoader.on(StopEvent) { stopEvent ->
			stop()
			logger.debug('Animation stopped')
		}

		graphicsEngine.on(EngineLoopStartEvent) { event ->
			animationLoader.load(animationFile).play()
			logger.debug('Animation started')
		}

		logger.info('Waiting for animation to finish.  Close the window to exit.')
	}

	/**
	 * Load a video for playing.
	 * 
	 * @param videoFile
	 */
	private void loadVideo(VideoFile videoFile) {

		def videoLoader = new VideoLoader(scene, graphicsEngine, inputEventStream, gameClock)
		videoLoader.on(StopEvent) { event ->
			stop()
			logger.debug('Video stopped')
		}

		graphicsEngine.on(EngineLoopStartEvent) { event ->
			videoLoader.load(videoFile).play()
			logger.debug('Video started')
		}

		logger.info('Waiting for video to finish.  Close the window to exit.')
	}

	@Override
	void run() {

		logger.info('File details: {}', mediaFile)

		switch (mediaFile) {
			case VideoFile:
				loadVideo(mediaFile)
				break
			case AnimationFile:
				loadAnimation(mediaFile)
				break
			case SoundFile:
				new SoundPlayer(audioConfig, mediaFile).start()
				break
			case ImageFile:
				new ImageViewer(graphicsConfig, mediaFile).start()
				break
			case ImagesFile:
				new ImagesViewer(graphicsConfig, mediaFile, paletteType).start()
				break
			default:
				logger.error('No media player for the associated file class of {}', mediaFile)
				throw new UnsupportedOperationException()
		}
	}
}
