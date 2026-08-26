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
class AudioSource extends AudioNode<AudioSource> implements EventTarget<AudioSource> {

	@Delegate(interfaces = false, includes = ['isLooping', 'isPaused', 'isPlaying', 'isStopped'])
	private final Source source
	private final StreamingAudioData streamingAudioData
	private final List<Buffer> readBuffers = []
	private final List<Buffer> streamedBuffers = []
	private int buffersProcessed
	private State state = State.STOPPED

	/**
	 * Constructor, create an audio source attached to streaming audio data.
	 */
	AudioSource(StreamingAudioData streamingAudioData) {

		source = new OpenALSource()
		this.streamingAudioData = streamingAudioData
	}

	/**
	 * Constructor, create an audio source attached to preloaded audio data.
	 */
	AudioSource(AudioData audioData) {

		source = new OpenALSource().attachBuffer(audioData.buffer)
		streamingAudioData = null
	}

	@Override
	void close() {

		source.close()
		streamedBuffers*.close()
		super.close()
	}

	/**
	 * Pause the sound.
	 */
	AudioSource pause() {

		source.pause()
		return this
	}

	/**
	 * Play the sound.
	 */
	AudioSource play() {

		if (streamingAudioData) {
			readBuffersFromStreamingSource()
		}
		source.play()
		return this
	}

	/**
	 * Load up the source queue with the next set of buffers from a streaming
	 * source.
	 */
	private void readBuffersFromStreamingSource() {

		readBuffers.clear()
		streamingAudioData.read(readBuffers, buffersProcessed)
		source.queueBuffers(*readBuffers)
		streamedBuffers.addAll(readBuffers)
	}

	@Override
	void render() {

		if (streamingAudioData) {
			readBuffersFromStreamingSource()
		}

		var currentState = source.paused ? State.PAUSED : source.playing ? State.PLAYING : State.STOPPED
		if (currentState != state) {
			switch (currentState) {
				case State.STOPPED -> trigger(new AudioStoppedEvent(this))
				case State.PLAYING -> trigger(new AudioPlayingEvent(this))
				case State.PAUSED -> trigger(new AudioPausedEvent(this))
			}
			state = currentState
		}

		source.withPosition(globalPosition)

		// Close any used buffers (n/a for looping tracks)
		if (streamingAudioData && !source.looping) {
			buffersProcessed = source.buffersProcessed()
			if (buffersProcessed) {
				var exhaustedBuffers = streamedBuffers.take(buffersProcessed)
				source.unqueueBuffers(*exhaustedBuffers)
				exhaustedBuffers*.close()
			}
		}
	}

	/**
	 * Stop the sound.
	 */
	AudioSource stop() {

		source.stop()
		return this
	}

	/**
	 * If this source is attached to streaming audio data, update that stream so
	 * that there are buffers to read at next render.
	 */
	void update() {

		streamingAudioData?.update()
	}

	/**
	 * Set the gain (ie: volume) of the sound.
	 */
	AudioSource withGain(float gain) {

		source.withGain(gain)
		return this
	}

	/**
	 * Set the maximum distance at which there will no longer be any attenuation
	 * of the sound.
	 */
	AudioSource withMaxDistance(float maxDistance) {

		source.withMaxDistance(maxDistance)
		return this
	}

	/**
	 * Set the distance at which the volume of the source would be cut by half
	 * (before being influenced by rolloff factor).
	 */
	AudioSource withReferenceDistance(float referenceDistance) {

		source.withReferenceDistance(referenceDistance)
		return this
	}

	/**
	 * Set the rolloff factor for sounds from this source.
	 */
	AudioSource withRolloff(float rolloff) {

		source.withRolloff(rolloff)
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
