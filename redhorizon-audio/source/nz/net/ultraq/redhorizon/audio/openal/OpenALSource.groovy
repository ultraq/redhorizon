/*
 * Copyright 2024, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.audio.openal

import nz.net.ultraq.redhorizon.audio.Buffer
import nz.net.ultraq.redhorizon.audio.Source

import static org.lwjgl.openal.AL10.*
import static org.lwjgl.openal.AL11.*

/**
 * OpenAL-specific source implementation.
 *
 * @author Emanuel Rabina
 */
class OpenALSource implements Source {

	final int sourceId

	/**
	 * Constructor, creates a new source to which buffers can be attached.
	 */
	OpenALSource() {

		sourceId = alGenSources()
	}

	@Override
	Source attachBuffer(Buffer buffer) {

		if (alGetSourcei(sourceId, AL_BUFFER) == 0) {
			alSourcei(sourceId, AL_BUFFER, ((OpenALBuffer)buffer).bufferId)
		}
		return this
	}

	@Override
	int buffersProcessed() {

		return alGetSourcei(sourceId, AL_BUFFERS_PROCESSED)
	}

	@Override
	void close() {

		alDeleteSources(sourceId)
	}

	/**
	 * Return this source's state.
	 */
	private int getSourceState() {

		return alGetSourcei(sourceId, AL_SOURCE_STATE)
	}

	@Override
	boolean isBufferAttached() {

		return alGetSourcei(sourceId, AL_BUFFER) != 0
	}

	@Override
	boolean isLooping() {

		return alGetSourcei(sourceId, AL_LOOPING) == AL_TRUE
	}

	@Override
	boolean isPaused() {

		return sourceState == AL_PAUSED
	}

	@Override
	boolean isPlaying() {

		return sourceState == AL_PLAYING
	}

	@Override
	boolean isStopped() {

		return sourceState == AL_STOPPED
	}

	@Override
	Source pause() {

		if (!paused) {
			alSourcePause(sourceId)
		}
		return this
	}

	@Override
	Source play() {

		if (!playing) {
			// Once a buffer is attached or several queued, the source state is one of
			// AL_STATIC or AL_STREAMING.  Disallow playing until this is known
			if (alGetSourcei(sourceId, AL_SOURCE_TYPE) != AL_UNDETERMINED) {
				alSourcePlay(sourceId)
			}
		}
		return this
	}

	@Override
	void queueBuffers(Buffer... buffers) {

		alSourceQueueBuffers(sourceId, *((OpenALBuffer[])buffers)*.bufferId)
	}

	@Override
	Source rewind() {

		alSourceRewind(sourceId)
		return this
	}

	@Override
	Source stop() {

		if (!stopped) {
			alSourceStop(sourceId)
		}
		return this
	}

	@Override
	void unqueueBuffers(Buffer... buffers) {

		alSourceUnqueueBuffers(sourceId, *((OpenALBuffer[])buffers)*.bufferId)
	}

	@Override
	Source withLooping(boolean looping) {

		alSourcei(sourceId, AL_LOOPING, looping ? AL_TRUE : AL_FALSE)
		return this
	}

	@Override
	Source withVolume(float volume) {

		alSourcef(sourceId, AL_GAIN, volume)
		return this
	}
}
