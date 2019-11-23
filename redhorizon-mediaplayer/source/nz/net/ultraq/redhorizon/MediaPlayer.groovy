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
import nz.net.ultraq.redhorizon.filetypes.SoundFile

import org.reflections.Reflections
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Basic media player, primarily used for testing the various file formats so
 * that I can check decoding has worked.
 * 
 * @author Emanuel Rabina
 */
class MediaPlayer {

	private static final Logger logger = LoggerFactory.getLogger(MediaPlayer)

	/**
	 * Launch the media player and present the given file.
	 * 
	 * @param args
	 */
	static void main(String[] args) {

		try {
			if (args.length == 0) {
				throw new IllegalArgumentException('A path to a file must be supplied')
			}

			def (pathToFile) = args
			new FileInputStream(pathToFile).withCloseable { input ->
				def soundFileClass = getSoundFileClass(pathToFile)
				def soundFile = soundFileClass.newInstance(input)
				def audioPlayer = new AudioPlayer(soundFile)
				audioPlayer.play()
			}
		}
		catch (Exception ex) {
			logger.error(ex.message, ex)
			System.exit(1)
		}
	}

	/**
	 * Find the {@link SoundFile} class that should be able to play the file at
	 * the given path.
	 * 
	 * @param pathToFile
	 * @return
	 */
	private static Class<SoundFile> getSoundFileClass(String pathToFile) {

		def suffix = pathToFile.substring(pathToFile.lastIndexOf('.') + 1)
		def soundFileClass = new Reflections('nz.net.ultraq.redhorizon.filetypes')
			.getTypesAnnotatedWith(FileExtensions)
			.find { type ->
				def annotation = type.getAnnotation(FileExtensions)
				return annotation.value().contains(suffix)
			}
		if (!soundFileClass) {
			throw new IllegalArgumentException("No implementation for ${suffix} filetype")
		}
		return soundFileClass
	}
}
