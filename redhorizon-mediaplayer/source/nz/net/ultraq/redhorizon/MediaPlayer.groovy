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

import nz.net.ultraq.redhorizon.filetypes.FileExtensions
import nz.net.ultraq.redhorizon.filetypes.ImageFile
import nz.net.ultraq.redhorizon.filetypes.SoundFile

import org.reflections.Reflections
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Parameters

import java.util.concurrent.Callable

/**
 * Basic media player, primarily used for testing the various file formats so
 * that I can check decoding has worked.
 * 
 * @author Emanuel Rabina
 */
@Command(name = "play", mixinStandardHelpOptions = true, version = '${sys:redhorizon.version}')
class MediaPlayer implements Callable<Integer> {

	private static final Logger logger = LoggerFactory.getLogger(MediaPlayer)

	@Parameters(index = '0', arity = '1', description = 'Path to the input file to play/view')
	String file

	/**
	 * Launch the media player and present the given file in the most appropriate
	 * manner.
	 * 
	 * @return
	 */
	@Override
	Integer call() {

		new FileInputStream(file).withCloseable { input ->
			def fileClass = getFileClass()
			def file = fileClass.newInstance(input)

			if (file instanceof SoundFile) {
				def audioPlayer = new AudioPlayer(file)
				audioPlayer.play()
			}
			else if (file instanceof ImageFile) {
				def imageViewer = new ImageViewer(file)
				imageViewer.view()
			}
			else {
				logger.error("No media player for the associated file class of ${fileClass}")
				throw new UnsupportedOperationException()
			}
		}
	}

	/**
	 * Find the appropriate file class for reading the file at the given path.
	 * 
	 * @return
	 */
	private Class<?> getFileClass() {

		def suffix = file.substring(file.lastIndexOf('.') + 1)
		def fileClass = new Reflections('nz.net.ultraq.redhorizon.filetypes')
			.getTypesAnnotatedWith(FileExtensions)
			.find { type ->
				def annotation = type.getAnnotation(FileExtensions)
				return annotation.value().contains(suffix)
			}
		if (!fileClass) {
			logger.error("No implementation for ${suffix} filetype")
			throw new IllegalArgumentException()
		}
		return fileClass
	}

	/**
	 * Bootstrap the application using Picocli.
	 * 
	 * @param args
	 */
	static void main(String[] args) {
//		System.err = new PrintStream(OutputStream.nullOutputStream())
//		System.out = new PrintStream(OutputStream.nullOutputStream())
		System.exit(new CommandLine(new MediaPlayer()).execute(args))
	}
}
