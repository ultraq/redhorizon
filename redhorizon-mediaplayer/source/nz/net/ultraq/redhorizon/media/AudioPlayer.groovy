/* 
 * Copyright 2019, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.media

import nz.net.ultraq.redhorizon.engine.audio.AudioEngine
import nz.net.ultraq.redhorizon.filetypes.SoundFile
import nz.net.ultraq.redhorizon.filetypes.aud.AudFile

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.transform.TupleConstructor
import java.util.concurrent.Executors

/**
 * A basic audio player, used primarily for testing purposes.
 * 
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class AudioPlayer {

	private static final Logger logger = LoggerFactory.getLogger(AudioPlayer)

	final SoundFile soundFile

	/**
	 * Play the configured audio file.
	 */
	void play() {

		logger.info('File details: {}', soundFile)

		Executors.newCachedThreadPool().executeAndShutdown { executorService ->

			// Try determine the appropriate media for the sound file
			def sound = soundFile instanceof AudFile && soundFile.uncompressedSize > 1048576 ? // 1MB
				new SoundTrack(soundFile, executorService) :
				new SoundEffect(soundFile)

			def audioEngine = new AudioEngine()
			audioEngine.addSceneElement(sound)

			def engine = executorService.submit(audioEngine)

			sound.on(StopEvent) { event ->
				logger.debug('Sound stopped')
				audioEngine.stop()
			}
			sound.play()

			logger.info('Waiting for sound to stop playing.  Press [Enter] to exit.')

			// TODO: I think this is what's holding up execution - see if I can't kill it on program end
			executorService.submit({ ->
				def reader = new InputStreamReader(System.in)
				if (reader.read()) {
					logger.debug('Keyboard input received')
					audioEngine.stop()
				}
			})

			engine.get()
		}
	}
}
