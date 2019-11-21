/* 
 * Copyright 2007, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine.audio

import nz.net.ultraq.redhorizon.geometry.Orientation
import nz.net.ultraq.redhorizon.geometry.Vector3f

import static org.lwjgl.openal.AL10.*

import java.nio.ByteBuffer

/**
 * An audio renderer using the OpenAL API.
 * 
 * @author Emanuel Rabina
 */
class OpenALRenderer implements AudioRenderer {

	/**
	 * Wrapper function to check for any OpenAL errors, throwing them if they
	 * occure.
	 * 
	 * @param methodName
	 * @param closure
	 */
	private static <T> T checkForError(String methodName, Closure closure) {

		def result = closure()
		def error = alGetError()
		if (error != AL_NO_ERROR) {
			def errorCode =
				error == AL_INVALID_NAME ? 'AL_INVALID_NAME' :
				error == AL_INVALID_ENUM ? 'AL_INVALID_ENUM' :
				error == AL_INVALID_VALUE ? 'AL_INVALID_VALUE' :
				error == AL_INVALID_OPERATION ? 'AL_INVALID_OPERATION' :
				error == AL_OUT_OF_MEMORY ? 'AL_OUT_OF_MEMORY' :
				error
			throw new Exception("OpenAL error in ${methodName}: ${errorCode}")
		}
		return result
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	int createBuffer(ByteBuffer data, int bitrate, int channels, int frequency) {

		return checkForError('createBuffer') { ->
			def bufferId = alGenBuffers()
			def format =
				bitrate == 8 && channels == 1 ? AL_FORMAT_MONO8 :
				bitrate == 8 && channels == 2 ? AL_FORMAT_STEREO8 :
				bitrate == 16 && channels == 1 ? AL_FORMAT_MONO16 :
				bitrate == 16 && channels == 2 ? AL_FORMAT_STEREO16 :
				0
			alBufferData(bufferId, format, data, frequency)
			return bufferId
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	int createSource() {

		return checkForError('createSource') { ->
			return alGenSources()
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void deleteBuffers(int[] bufferIds) {

		checkForError('deleteBuffers') { ->
			alDeleteBuffers(bufferIds)
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void deleteSource(int sourceId) {

		checkForError('deleteSource') { ->
			alDeleteSources(sourceId)
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void playSource(int sourceId) {

		checkForError('playSource') { ->
			alSourcePlay(sourceId)
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void queueBuffer(int sourceId, int bufferId) {

		checkForError('queueBuffer') { ->
			alSourceQueueBuffers(sourceId, bufferId)
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	boolean sourceExists(int sourceId) {

		return checkForError('sourceExists') { ->
			return alIsSource(sourceId)
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	boolean sourcePlaying(int sourceId) {

		return checkForError('sourcePlaying') { ->
			def state = alGetSourcei(sourceId, AL_SOURCE_STATE)
			return state == AL_PLAYING
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void updateListener(Vector3f position, Vector3f velocity, Orientation orientation) {

		checkForError('updateListener') { ->
			alListenerfv(AL_POSITION, position as float[])
			alListenerfv(AL_VELOCITY, velocity as float[])
			alListenerfv(AL_ORIENTATION, orientation as float[])
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void updateSource(int sourceId, Vector3f position, Vector3f direction, Vector3f velocity) {

		checkForError('updateSource') { ->
			alSourcefv(sourceId, AL_POSITION, position as float[])
			alSourcefv(sourceId, AL_DIRECTION, direction as float[])
			alSourcefv(sourceId, AL_VELOCITY, velocity as float[])
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void updateVolume(float volume) {

		checkForError('updateVolume') { ->
			alListenerf(AL_GAIN, volume)
		}
	}
}
