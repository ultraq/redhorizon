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

package nz.net.ultraq.redhorizon.utilities

import nz.net.ultraq.redhorizon.classic.PaletteTypes
import nz.net.ultraq.redhorizon.classic.filetypes.mix.MixFile
import nz.net.ultraq.redhorizon.filetypes.AnimationFile
import nz.net.ultraq.redhorizon.filetypes.FileExtensions
import nz.net.ultraq.redhorizon.filetypes.ImageFile
import nz.net.ultraq.redhorizon.filetypes.ImagesFile
import nz.net.ultraq.redhorizon.filetypes.SoundFile
import nz.net.ultraq.redhorizon.filetypes.VideoFile
import nz.net.ultraq.redhorizon.utilities.mediaplayer.AnimationPlayer
import nz.net.ultraq.redhorizon.utilities.mediaplayer.SoundPlayer
import nz.net.ultraq.redhorizon.utilities.mediaplayer.ImageViewer
import nz.net.ultraq.redhorizon.utilities.mediaplayer.ImagesViewer
import nz.net.ultraq.redhorizon.utilities.mediaplayer.VideoPlayer

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

	@Parameters(index = '0', description = 'Path to the input file to play/view')
	File file

	@Parameters(index = '1', arity = '0..1', description = 'If <file> is a mix file, this is the name of the entry in the mix file to play')
	String entryName

	@Option(names = ['--filter'], description = 'Use nearest-neighbour filtering to smooth the appearance of images')
	boolean filter

	@Option(names = ['--fix-aspect-ratio'], description = 'Adjust the aspect ratio for modern displays')
	boolean fixAspectRatio

	@Option(names = ['--full-screen'], description = 'Run in fullscreen mode')
	boolean fullScreen

	@Option(names = ['--palette'], defaultValue = 'ra-temperate', description = 'Which game palette to apply to a paletted image.  One of "ra-snow", "ra-temperate", or "td-temperate".  Defaults to ra-temperate')
	PaletteTypes paletteType

	@Option(names = ['--scale-low-res'], description = 'Double the output resolution of low-res animations and videos (320x200 or lower).  Useful in conjunction with filtering so that the result is still filtered but less blurry.')
	boolean scaleLowRes

	@Option(names = ['--scanlines'], description = 'Add scanlines to the image, emulating the look of images on CRT displays')
	boolean scanlines

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
		if (file.name.endsWith('.mix')) {
			new MixFile(file).withCloseable { mix ->
				def entry = mix.getEntry(entryName)
				if (entry) {
					logger.info('Loading {}...', entryName)
					mix.getEntryData(entry).withBufferedStream { inputStream ->
						play(getFileClass(entryName).newInstance(inputStream))
					}
				}
				else {
					logger.error('{} not found in {}', entryName, file)
					throw new IllegalArgumentException()
				}
			}
		}
		else {
			file.withInputStream { inputStream ->
				play(getFileClass(file.name).newInstance(inputStream))
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
		def fileClass = new Reflections(
			'nz.net.ultraq.redhorizon.filetypes',
			'nz.net.ultraq.redhorizon.classic.filetypes'
		)
			.getTypesAnnotatedWith(FileExtensions)
			.find { type ->
				def annotation = type.getAnnotation(FileExtensions)
				return annotation.value().any { extension ->
					return extension.equalsIgnoreCase(suffix)
				}
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
				def videoPlayer = new VideoPlayer(file, filter, fixAspectRatio, fullScreen, scaleLowRes, scanlines)
				videoPlayer.play()
				break
			case AnimationFile:
				def animationPlayer = new AnimationPlayer(file, filter, fixAspectRatio, fullScreen, scaleLowRes, scanlines)
				animationPlayer.play()
				break
			case SoundFile:
				def soundPlayer = new SoundPlayer(file)
				soundPlayer.play()
				break
			case ImageFile:
				def imageViewer = new ImageViewer(file, filter, fixAspectRatio, fullScreen)
				imageViewer.view()
				break
			case ImagesFile:
				def imagesViewer = new ImagesViewer(file, filter, fixAspectRatio, fullScreen, paletteType)
				imagesViewer.view()
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
		System.exit(
			new CommandLine(new MediaPlayer())
				.registerConverter(PaletteTypes, { value ->
					return PaletteTypes.find { paletteType ->
						return value == paletteType.name().toLowerCase().replaceAll('_', '-')
					}
				})
				.setCaseInsensitiveEnumValuesAllowed(true)
				.execute(args)
		)
	}
}
