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
import nz.net.ultraq.redhorizon.engine.audio.AudioConfiguration
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

	@Mixin
	GraphicsOptions graphicsOptions

	@Option(names = ['--volume'], defaultValue = '50', description = 'The volume level, as a number from 0-100')
	int volume

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

		fileOptions.useFile(logger) { mediaFile ->
			def graphicsConfig = graphicsOptions.asGraphicsConfiguration()
			def audioConfig = new AudioConfiguration(
				volume: volume / 100
			)

			switch (mediaFile) {
				case VideoFile:
					new VideoPlayer(audioConfig, graphicsConfig, mediaFile).start()
					break
				case AnimationFile:
					new AnimationPlayer(graphicsConfig, mediaFile).start()
					break
				case SoundFile:
					new SoundPlayer(audioConfig, mediaFile).start()
					break
				case ImageFile:
					new ImageViewer(graphicsConfig, mediaFile).start()
					break
				case ImagesFile:
					new ImagesViewer(graphicsConfig, mediaFile, paletteOptions.paletteType).start()
					break
				default:
					logger.error('No media player for the associated file class of {}', mediaFile)
					throw new UnsupportedOperationException()
			}
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
