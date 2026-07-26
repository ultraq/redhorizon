/*
 * Copyright 2026, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.audio

import nz.net.ultraq.eventhorizon.EventTarget
import nz.net.ultraq.redhorizon.audio.openal.OpenALSource

/**
 * A point in the scene from which audio can be played.  Used to give audio
 * positional qualities.
 *
 * @author Emanuel Rabina
 */
class Source extends AudioNode<Source> implements EventTarget<Source> {

	private final nz.net.ultraq.redhorizon.audio.api.Source source
	private final Music music

	/**
	 * Constructor, create an audio source attached to sound effect data.
	 */
	Source(Sound sound) {

		source = new OpenALSource().attachBuffer(sound.buffer)
		music = null
	}

	/**
	 * Constructor, create an audio source attached to streaming sound data.
	 */
	Source(Music music) {

		source = new OpenALSource()
		music.update(source)
		this.music = music
	}

	@Override
	void close() {

		source.close()
		super.close()
	}

	/**
	 * Return whether the sound is currently paused.
	 */
	boolean isPaused() {

		return source.isPaused()
	}

	/**
	 * Return whether the sound is currently playing.
	 */
	boolean isPlaying() {

		return source.isPlaying()
	}

	/**
	 * Return whether the sound is currently stopped.
	 */
	boolean isStopped() {

		return source.isStopped()
	}

	/**
	 * Pause the sound.
	 */
	Source pause() {

		source.pause()
		return this
	}

	/**
	 * Play the sound.
	 */
	Source play() {

		source.play()
		return this
	}

	@Override
	void render() {

		music?.update(source)
		source.withPosition(globalPosition)
	}

	/**
	 * Stop the sound.
	 */
	Source stop() {

		source.stop()
		return this
	}

	/**
	 * Set the volume of the sound.
	 */
	Source withVolume(float volume) {

		source.withVolume(volume)
		return this
	}
}
