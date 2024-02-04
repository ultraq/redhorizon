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

package nz.net.ultraq.redhorizon.engine.audio.openal

import nz.net.ultraq.redhorizon.engine.audio.Buffer
import nz.net.ultraq.redhorizon.engine.audio.Source

import static org.lwjgl.openal.AL10.*
import static org.lwjgl.openal.AL11.AL_UNDETERMINED

/**
 * OpenAL-specific source implementation.
 *
 * @author Emanuel Rabina
 */
class OpenALSource extends Source {

	final int sourceId

	// TODO: Currently using a call to the sound hardware for every query, could
	//       possibly be optimized by storing a local value instead.  This doesn't
	//       seem to be the bottleneck for us now, so leaving as is.

	/**
	 * Constructor, creates a new source to which buffers can be attached  later.
	 */
	OpenALSource() {

		sourceId = alGenSources()
	}

	@Override
	void attachBuffer(Buffer buffer) {

		if (alGetSourcei(sourceId, AL_BUFFER) == 0) {
			alSourcei(sourceId, AL_BUFFER, ((OpenALBuffer)buffer).bufferId)
		}
	}

	@Override
	void close() {

		alDeleteSources(sourceId)
	}

	/**
	 * Return this source's state.
	 *
	 * @return
	 */
	private int getSourceState() {

		return alGetSourcei(sourceId, AL_SOURCE_STATE)
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
	void pause() {

		if (!paused) {
			alSourcePause(sourceId)
		}
	}

	@Override
	void play() {

		if (!playing) {
			// Once a buffer is attached or several queued, the source state is one of
			// AL_STATIC or AL_STREAMING.  Disallow playing until this is known
			if (alGetSourcei(sourceId, AL_SOURCE_TYPE) != AL_UNDETERMINED) {
				alSourcePlay(sourceId)
			}
		}
	}

	@Override
	void queueBuffers(Buffer... buffers) {

		alSourceQueueBuffers(sourceId, ((OpenALBuffer[])buffers)*.bufferId as int[])
	}

	@Override
	void rewind() {

		alSourceRewind(sourceId)
	}

	@Override
	void stop() {

		if (!stopped) {
			alSourceStop(sourceId)
		}
	}
}
