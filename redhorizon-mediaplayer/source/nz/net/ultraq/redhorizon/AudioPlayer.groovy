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

package nz.net.ultraq.redhorizon

import nz.net.ultraq.redhorizon.engine.audio.AudioEngine
import nz.net.ultraq.redhorizon.filetypes.SoundFile
import nz.net.ultraq.redhorizon.media.SoundEffect

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.transform.TupleConstructor
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory

/**
 * A basic audio player, used primarily for testing purposes.
 * 
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class AudioPlayer {

	private static final Logger logger = LoggerFactory.getLogger(AudioPlayer)

	final SoundFile file

	/**
	 * Play the configured audio file.
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
