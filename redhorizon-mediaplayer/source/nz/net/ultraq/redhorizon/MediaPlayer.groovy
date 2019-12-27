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

package nz.net.ultraq.redhorizon

import nz.net.ultraq.redhorizon.filetypes.AnimationFile
import nz.net.ultraq.redhorizon.filetypes.FileExtensions
import nz.net.ultraq.redhorizon.filetypes.ImageFile
import nz.net.ultraq.redhorizon.filetypes.SoundFile
import nz.net.ultraq.redhorizon.filetypes.VideoFile
import nz.net.ultraq.redhorizon.filetypes.mix.MixFile
import nz.net.ultraq.redhorizon.media.AnimationPlayer
import nz.net.ultraq.redhorizon.media.AudioPlayer
import nz.net.ultraq.redhorizon.media.ImageViewer
import nz.net.ultraq.redhorizon.media.VideoPlayer

import org.reflections.Reflections
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Model.CommandSpec
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
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
	mixinStandardHelpOptions = true,
	version = '${sys:redhorizon.version}'
)
class MediaPlayer implements Callable<Integer> {

	private static final Logger logger = LoggerFactory.getLogger(MediaPlayer)

	@Spec
	CommandSpec commandSpec

	@Parameters(index = '0', arity = '1', description = 'Path to the input file to play/view')
	String file

	@Parameters(index = '1', arity = '0..1', description = 'If <file> is a mix file, this is the name of the entry in the mix file to play')
	String entryName

	@Option(names = ['--fix-aspect-ratio'], description = 'Adjust the aspect ratio for modern displays (images/animations/videos only)')
	boolean fixAspectRatio

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

		logger.info('Loading {}...', file)
		if (file.endsWith('.mix')) {
			new MixFile(new File(file)).withCloseable { mix ->
				def entry = mix.getEntry(entryName)
				if (entry) {
					logger.info('Loading {}...', entryName)
					new BufferedInputStream(mix.getEntryData(entry)).withCloseable { entryInput ->
						play(getFileClass(entryName).newInstance(entryInput))
					}
				}
				else {
					logger.error('{} not found in {}', entryName, file)
					throw new IllegalArgumentException()
				}
			}
		}
		else {
			new BufferedInputStream(new FileInputStream(file)).withCloseable { input ->
				play(getFileClass(file).newInstance(input))
			}
		}
		return 0
	}

	/**
	 * Find the appropriate class for reading a file with the given name.
	 * 
	 * @param filename
	 * @return
	 */
	private Class<?> getFileClass(String filename) {

		def suffix = filename.substring(filename.lastIndexOf('.') + 1)
		def fileClass = new Reflections('nz.net.ultraq.redhorizon.filetypes')
			.getTypesAnnotatedWith(FileExtensions)
			.find { type ->
				def annotation = type.getAnnotation(FileExtensions)
				return annotation.value().contains(suffix)
			}
		if (!fileClass) {
			logger.error('No implementation for {} filetype', suffix)
			throw new IllegalArgumentException()
		}
		return fileClass
	}

	/**
	 * Launch the appropriate media player for the given file.
	 * 
	 * @param file
	 */
	private void play(Object file) {

		switch (file) {
			case VideoFile:
				def videoPlayer = new VideoPlayer(file, fixAspectRatio)
				videoPlayer.play()
				break
			case AnimationFile:
				def animationPlayer = new AnimationPlayer(file, fixAspectRatio)
				animationPlayer.play()
				break
			case SoundFile:
				def audioPlayer = new AudioPlayer(file)
				audioPlayer.play()
				break
			case ImageFile:
				def imageViewer = new ImageViewer(file, fixAspectRatio)
				imageViewer.view()
				break
			default:
				logger.error('No media player for the associated file class of {}', file)
				throw new UnsupportedOperationException()
		}
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
