/* 
 * Copyright 2016, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.cli


import nz.net.ultraq.redhorizon.cli.mediaplayer.AnimationPlayer
import nz.net.ultraq.redhorizon.cli.mediaplayer.SoundPlayer
import nz.net.ultraq.redhorizon.cli.mediaplayer.ImageViewer
import nz.net.ultraq.redhorizon.cli.mediaplayer.ImagesViewer
import nz.net.ultraq.redhorizon.cli.mediaplayer.VideoPlayer
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsConfiguration
import nz.net.ultraq.redhorizon.filetypes.AnimationFile
import nz.net.ultraq.redhorizon.filetypes.ImageFile
import nz.net.ultraq.redhorizon.filetypes.ImagesFile
import nz.net.ultraq.redhorizon.filetypes.SoundFile
import nz.net.ultraq.redhorizon.filetypes.VideoFile

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Mixin
import picocli.CommandLine.Model.CommandSpec
import picocli.CommandLine.Option
import picocli.CommandLine.Spec

import java.util.concurrent.Callable

/**
 * Basic media player, primarily used for testing the various file formats so
 * that I can check decoding has worked.
 * 
 * @author Emanuel Rabina
 */
@Command(
	name = "play",
	header = [
		'',
		'Red Horizon Media Player',
		'========================',
		''
	],
	description = 'Play/View a variety of supported media formats',
	mixinStandardHelpOptions = true,
	version = '${sys:redhorizon.version}'
)
class MediaPlayer implements Callable<Integer> {

	private static final Logger logger = LoggerFactory.getLogger(MediaPlayer)

	@Spec
	CommandSpec commandSpec

	@Mixin
	FileOptions fileOptions

	@Option(names = ['--filter'], description = 'Use nearest-neighbour filtering to smooth the appearance of images')
	boolean filter

	@Option(names = ['--fix-aspect-ratio'], description = 'Adjust the aspect ratio for modern displays')
	boolean fixAspectRatio

	@Option(names = ['--full-screen'], description = 'Run in fullscreen mode')
	boolean fullScreen

	@Option(names = ['--scale-low-res'], description = 'Double the output resolution of low-res animations and videos (320x200 or lower).  Useful in conjunction with filtering so that the result is still filtered but less blurry.')
	boolean scaleLowRes

	@Option(names = ['--scanlines'], description = 'Add scanlines to the image, emulating the look of images on CRT displays')
	boolean scanlines

	@Mixin
	PaletteOptions paletteOptions

	/**
	 * Launch the media player and present the given file in the most appropriate
	 * manner.
	 * 
	 * @return
	 */
	@Override
	Integer call() {

		Thread.currentThread().name = 'Media Player [main]'
		logger.info('Red Horizon Media Player {}', commandSpec.version()[0] ?: '(development)')

		def mediaFile = fileOptions.loadFile(logger)
		def graphicsConfig = new GraphicsConfiguration(
			filter: filter,
			fixAspectRatio: fixAspectRatio,
			fullScreen: fullScreen
		)

		switch (mediaFile) {
		case VideoFile:
			def videoPlayer = new VideoPlayer(mediaFile, graphicsConfig, scaleLowRes, scanlines)
			videoPlayer.play()
			break
		case AnimationFile:
			def animationPlayer = new AnimationPlayer(mediaFile, graphicsConfig, scaleLowRes, scanlines)
			animationPlayer.play()
			break
		case SoundFile:
			def soundPlayer = new SoundPlayer(mediaFile)
			soundPlayer.play()
			break
		case ImageFile:
			def imageViewer = new ImageViewer(mediaFile, graphicsConfig)
			imageViewer.view()
			break
		case ImagesFile:
			def imagesViewer = new ImagesViewer(mediaFile, graphicsConfig, paletteOptions.paletteType)
			imagesViewer.view()
			break
		default:
			logger.error('No media player for the associated file class of {}', mediaFile)
			throw new UnsupportedOperationException()
		}

		return 0
	}

	/**
	 * Bootstrap the application using Picocli.
	 * 
	 * @param args
	 */
	static void main(String[] args) {
		System.exit(new CommandLine(new MediaPlayer()).execute(args))
	}
}
