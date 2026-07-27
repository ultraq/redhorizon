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
class SourceNode extends AudioNode<SourceNode> implements EventTarget<SourceNode> {

	private final Source source
	private final Sound sound
	private final Music music
	private State state = State.STOPPED

	/**
	 * Constructor, create an audio source attached to sound effect data.
	 */
	SourceNode(Sound sound) {

		source = new OpenALSource().attachBuffer(sound.buffer)
		this.sound = sound
		music = null
	}

	/**
	 * Constructor, create an audio source attached to streaming sound data.
	 */
	SourceNode(Music music) {

		source = new OpenALSource()
		music.update(source)
		this.music = music
		sound = null
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
	SourceNode pause() {

		source.pause()
		return this
	}

	/**
	 * Play the sound.
	 */
	SourceNode play() {

		source.play()
		return this
	}

	@Override
	void render() {

		var currentState = source.paused ? State.PAUSED : source.playing ? State.PLAYING : State.STOPPED
		if (currentState != state) {
			switch (currentState) {
				case State.STOPPED -> trigger(new AudioStoppedEvent(this))
				case State.PLAYING -> trigger(new AudioPlayingEvent(this))
				case State.PAUSED -> trigger(new AudioPausedEvent(this))
			}
			state = currentState
		}

		music?.update(source)
		source.withPosition(globalPosition)
	}

	/**
	 * Stop the sound.
	 */
	SourceNode stop() {

		source.stop()
		return this
	}

	/**
	 * Set the maximum distance at which there will no longer be any attenuation
	 * of the sound.
	 */
	SourceNode withMaxDistance(float maxDistance) {

		source.withMaxDistance(maxDistance)
		return this
	}

	/**
	 * Set the distance at which the volume of the source would be cut by half
	 * (before being influenced by rolloff factor).
	 */
	SourceNode withReferenceDistance(float referenceDistance) {

		source.withReferenceDistance(referenceDistance)
		return this
	}

	/**
	 * Set the rolloff factor for sounds from this source.
	 */
	SourceNode withRolloff(float rolloff) {

		source.withRolloff(rolloff)
		return this
	}

	/**
	 * Set the volume of the sound.
	 */
	SourceNode withVolume(float volume) {

		source.withVolume(volume)
		return this
	}

	/**
	 * For tracking changes in source state.
	 */
	private static enum State {

		STOPPED,
		PLAYING,
		PAUSED
	}
}
