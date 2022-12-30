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

package nz.net.ultraq.redhorizon.cli.mediaplayer

import nz.net.ultraq.redhorizon.cli.AudioOptions
import nz.net.ultraq.redhorizon.cli.FileOptions
import nz.net.ultraq.redhorizon.cli.GraphicsOptions
import nz.net.ultraq.redhorizon.cli.PaletteOptions
import nz.net.ultraq.redhorizon.engine.geometry.Dimension

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine.Command
import picocli.CommandLine.Mixin
import picocli.CommandLine.Model.CommandSpec
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
	mixinStandardHelpOptions = true
)
class MediaPlayerCli implements Callable<Integer> {

	private static final Logger logger = LoggerFactory.getLogger(MediaPlayerCli)

	@Spec
	CommandSpec commandSpec

	@Mixin
	FileOptions fileOptions

	@Mixin
	GraphicsOptions graphicsOptions

	@Mixin
	AudioOptions audioOptions

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
		logger.info('Red Horizon Media Player {}', commandSpec.parent().version()[0])

		def audioConfig = audioOptions.asAudioConfiguration()
		def graphicsConfig = graphicsOptions.asGraphicsConfiguration(
			renderResolution: new Dimension(1280, 800)
		)

		fileOptions.useFile(logger) { mediaFile ->
			new MediaPlayer(mediaFile, audioConfig, graphicsConfig, paletteOptions.loadPalette()).start()
		}

		return 0
	}
}
