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

package nz.net.ultraq.redhorizon.mediaplayer

import nz.net.ultraq.redhorizon.engine.audio.AudioEngine
import nz.net.ultraq.redhorizon.filetypes.FileExtensions
import nz.net.ultraq.redhorizon.filetypes.SoundFile
import nz.net.ultraq.redhorizon.media.SoundEffect

import org.reflections.Reflections
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory

/**
 * Basic media player, primarily used for presenting the data of the various
 * file formats so that we can check that decoding has worked.
 * 
 * @author Emanuel Rabina
 */
class MediaPlayer {

	private static final Logger logger = LoggerFactory.getLogger(MediaPlayer)

	/**
	 * Launch the media player and play the given file or media data.
	 * 
	 * @param args
	 */
	static void main(String[] args) {

		try {
			if (args.length == 0) {
				throw new IllegalArgumentException('A path to a file must be supplied')
			}

			def soundFile = loadSoundFile(args[0])
			soundFile.withCloseable { file ->
				def mediaPlayer = new MediaPlayer(file)
				mediaPlayer.play()
			}
		}
		catch (Exception ex) {
			logger.error(ex.message, ex)
			System.exit(1)
		}
	}

	/**
	 * Read a file path and return it as a sound file.
	 * 
	 * @param pathToFile
	 * @return
	 */
	private static SoundFile loadSoundFile(String pathToFile) {

		def suffix = pathToFile.substring(pathToFile.lastIndexOf('.') + 1)
		def soundFileClass = new Reflections('nz.net.ultraq.redhorizon.filetypes')
			.getTypesAnnotatedWith(FileExtensions)
			.find { type ->
				def annotation = type.getAnnotation(FileExtensions)
				return annotation.value().contains(suffix)
			}

		if (soundFileClass) {
			return soundFileClass.newInstance(pathToFile, Files.newByteChannel(Paths.get(pathToFile)))
		}

		throw new IllegalArgumentException("No implementation for ${suffix} filetype")
	}


	private final SoundFile file

	/**
	 * Constructor, sets up the engine subsystems needed for playback.  Right now
	 * just creates an OpenAL context to play a sound file through.
	 * 
	 * @param file
	 */
	MediaPlayer(SoundFile file) {

		this.file = file
	}

	/**
	 * Play the audio data coming out of the configured audio channel.
	 */
	void play() {

		Throwable exception
		def defaultThreadFactory = Executors.defaultThreadFactory()
		def threadFactory = new ThreadFactory() {
			@Override
			Thread newThread(Runnable r) {
				def thread = defaultThreadFactory.newThread(r)
				thread.setUncaughtExceptionHandler({ t, e ->
					logger.error("Error on thread ${t.name}", e)
					exception = e
				})
				return thread
			}
		}

		Executors.newCachedThreadPool(threadFactory).executeAndShutdown { executorService ->
			def soundEffect = new SoundEffect(file, executorService)
			def audioEngine = new AudioEngine(soundEffect)

			executorService.execute(audioEngine)

			soundEffect.play()

			// TODO: This is dumb waiting.  Emit some sort of event or some way we can
			//       wait on the sound to stop playing.
			logger.debug('Waiting for sound to stop playing')
			while (exception == null) {
				Thread.sleep(500)
				if (!soundEffect.playing) {
					break
				}
			}

			audioEngine.stop()
			if (exception != null) {
				throw exception
			}
		}
	}
}
