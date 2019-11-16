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

import nz.net.ultraq.redhorizon.filetypes.aud.AudFile
import nz.net.ultraq.redhorizon.engine.audio.AudioEngine
import nz.net.ultraq.redhorizon.media.SoundEffect
import nz.net.ultraq.redhorizon.scenegraph.Scene

import java.nio.channels.FileChannel
import java.nio.file.Paths

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

		def (pathToFile) = args
		def mediaPlayer = new MediaPlayer(pathToFile)
		mediaPlayer.play()
	}


	private final nz.net.ultraq.redhorizon.filetypes.File file

	// TODO: Do I really need a scene for singular objects?  Can I create a
	//       super-simple type of scene for these sorts of things?
	private Scene scene = new Scene()
	private AudioEngine audioEngine = new AudioEngine(scene)

	/**
	 * Constructor, sets up the engine subsystems needed for playback.  Right now
	 * just creates an OpenAL context to play a sound file through.
	 * 
	 * @param pathToFile
	 */
	MediaPlayer(String pathToFile) {

		// TODO: Figure out some sort of pluggable structure so that I don't have to
		//       guess which file implementations to use based on file extension!
		def suffix = pathToFile.substring(pathToFile.indexOf('.'))
		switch (suffix) {
		case '.aud':

			// TODO: Should I push file opening down to the implementation too?  It
			//       feels weird specifying how to get the file when it can have a
			//       URI that can be used to determine what to use
			file = new AudFile(pathToFile, FileChannel.open(Paths.get(pathToFile)))
			break
		}
	}

	/**
	 * 
	 */
	void play() {

		def soundEffect = new SoundEffect(file)
		scene.rootnode.addChild(soundEffect)

		audioEngine.start()
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
