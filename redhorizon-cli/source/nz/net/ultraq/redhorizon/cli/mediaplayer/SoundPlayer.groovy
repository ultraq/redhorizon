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

package nz.net.ultraq.redhorizon.cli.mediaplayer

import nz.net.ultraq.redhorizon.Application
import nz.net.ultraq.redhorizon.classic.filetypes.aud.AudFile
import nz.net.ultraq.redhorizon.engine.audio.AudioConfiguration
import nz.net.ultraq.redhorizon.filetypes.SoundFile
import nz.net.ultraq.redhorizon.media.SoundEffect
import nz.net.ultraq.redhorizon.media.SoundTrack
import nz.net.ultraq.redhorizon.media.StopEvent
import nz.net.ultraq.redhorizon.scenegraph.Scene

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
class SoundPlayer extends Application {

	private static final Logger logger = LoggerFactory.getLogger(SoundPlayer)

	final SoundFile soundFile
	final AudioConfiguration audioConfig

	/**
	 * Play the configured audio file.
	 */
	void play() {

		logger.info('File details: {}', soundFile)

		def scene = new Scene()

		Executors.newCachedThreadPool().executeAndShutdown { executorService ->
			useGameClock(executorService) { gameClock ->
				useAudioEngine(scene, executorService, audioConfig) { audioEngine ->

					// Try determine the appropriate media for the sound file
					def sound = soundFile instanceof AudFile && soundFile.uncompressedSize > 1048576 ? // 1MB
						new SoundTrack(soundFile, gameClock, executorService) :
						new SoundEffect(soundFile)
					scene << sound

					sound.on(StopEvent) { event ->
						logger.debug('Sound stopped')
						audioEngine.stop()
					}
					sound.play()

					logger.info('Waiting for sound to stop playing.  Press [Enter] to exit.')

					// TODO: I think this is what's holding up execution - see if I can't kill it on program end
					executorService.submit({ ->
						Thread.currentThread().name = 'Input receiver'
						def reader = new InputStreamReader(System.in)
						if (reader.read()) {
							logger.debug('Keyboard input received')
							audioEngine.stop()
						}
					})
				}
			}
		}
	}
}
