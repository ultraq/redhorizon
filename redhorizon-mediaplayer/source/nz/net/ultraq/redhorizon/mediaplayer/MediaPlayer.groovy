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
import nz.net.ultraq.redhorizon.scenegraph.Scene

import org.reflections.Reflections

import java.nio.channels.FileChannel
import java.nio.file.Paths
import java.util.concurrent.Executors

/**
 * Basic media player, primarily used for presenting the data of the various
 * file formats so that we can check that decoding has worked.
 * 
 * @author Emanuel Rabina
 */
class MediaPlayer {

	/**
	 * Launch the media player and play the given file or media data.
	 * 
	 * @param args
	 */
	static void main(String[] args) {

		if (args.length == 0) {
			throw new IllegalArgumentException('A path to a file must be supplied')
		}

		def (pathToFile) = args
		def mediaPlayer = new MediaPlayer(pathToFile)
		mediaPlayer.play()
	}


	private final SoundFile file

	/**
	 * Constructor, sets up the engine subsystems needed for playback.  Right now
	 * just creates an OpenAL context to play a sound file through.
	 * 
	 * @param pathToFile
	 */
	MediaPlayer(String pathToFile) {

		def suffix = pathToFile.substring(pathToFile.lastIndexOf('.'))
		def soundFileClass = new Reflections('nz.net.ultraq.redhorizon.filetypes')
			.getTypesAnnotatedWith(FileExtensions)
			.find { type ->
				def annotation = type.getAnnotation(FileExtensions)
				return annotation.value().contains(suffix)
			}

		if (soundFileClass) {
			file = soundFileClass.newInstance(pathToFile, FileChannel.open(Paths.get(pathToFile)))
		}
		else {
			throw new IllegalArgumentException("No implementation for ${suffix} filetype")
		}
	}

	/**
	 * Play the audio data coming out of the configured audio channel.
	 */
	void play() {

		def scene = new Scene()
		def audioEngine = new AudioEngine(scene)

		Executors.newCachedThreadPool().executeAndShutdown { executorService ->
			executorService.execute(audioEngine)

			def soundEffect = new SoundEffect(file, executorService)
			scene.root.addChild(soundEffect)

			soundEffect.play()

			// TODO: This is dumb waiting.  Emit some sort of event or some way we can
			//       wait on the sound to stop playing.
			while (true) {
				Thread.sleep(500)
				if (!soundEffect.playing) {
					break
				}
			}

			audioEngine.stop()
		}
	}
}
