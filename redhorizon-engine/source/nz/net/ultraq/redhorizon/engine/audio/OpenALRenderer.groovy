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

import org.joml.Vector3f
import static org.lwjgl.openal.AL10.*
import static org.lwjgl.system.MemoryStack.stackPush

import java.nio.ByteBuffer

/**
 * An audio renderer using the OpenAL API.
 * 
 * @author Emanuel Rabina
 */
class OpenALRenderer implements AudioRenderer {

	/**
	 * Check for any OpenAL errors created by the OpenAL call in the given
	 * closure, throwing them if they occur.
	 * 
	 * @param closure
	 */
	private static <T> T checkForError(Closure<T> closure) {

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
			throw new Exception("OpenAL error: ${errorCode}")
		}
		return result
	}

	@Override
	void attachBufferToSource(int sourceId, int bufferId) {

		checkForError { -> alSourcei(sourceId, AL_BUFFER, bufferId) }
	}

	@Override
	int buffersProcessed(int sourceId) {

		return checkForError { -> alGetSourcei(sourceId, AL_BUFFERS_PROCESSED) }
	}

	@Override
	int createBuffer(ByteBuffer data, int bits, int channels, int frequency) {

		return stackPush().withCloseable { stack ->
			int bufferId = checkForError { -> alGenBuffers() }
			def format =
				bits == 8 && channels == 1 ? AL_FORMAT_MONO8 :
				bits == 8 && channels == 2 ? AL_FORMAT_STEREO8 :
				bits == 16 && channels == 1 ? AL_FORMAT_MONO16 :
				bits == 16 && channels == 2 ? AL_FORMAT_STEREO16 :
				0
			def soundBuffer = stack.malloc(data.capacity())
				.put(data)
				.flip()
			data.rewind()
			checkForError { -> alBufferData(bufferId, format, soundBuffer, frequency) }
			return bufferId
		}
	}

	@Override
	int createSource() {

		return checkForError { -> alGenSources() }
	}

	@Override
	void deleteBuffers(int... bufferIds) {

		checkForError { -> alDeleteBuffers(bufferIds) }
	}

	@Override
	void deleteSource(int sourceId) {

		checkForError { -> alDeleteSources(sourceId) }
	}

	@Override
	void pauseSource(int sourceId) {

		checkForError { -> alSourcePause(sourceId) }
	}

	@Override
	void playSource(int sourceId) {

		checkForError { -> alSourcePlay(sourceId) }
	}

	@Override
	void queueBuffers(int sourceId, int... bufferIds) {

		checkForError { -> alSourceQueueBuffers(sourceId, bufferIds) }
	}

	@Override
	boolean sourceExists(int sourceId) {

		return checkForError { -> alIsSource(sourceId) }
	}

	@Override
	boolean sourcePaused(int sourceId) {

		return checkForError { -> alGetSourcei(sourceId, AL_SOURCE_STATE) == AL_PAUSED }
	}

	@Override
	boolean sourcePlaying(int sourceId) {

		return checkForError { -> alGetSourcei(sourceId, AL_SOURCE_STATE) == AL_PLAYING }
	}

	@Override
	boolean sourceStopped(int sourceId) {

		return checkForError { -> alGetSourcei(sourceId, AL_SOURCE_STATE) == AL_STOPPED }
	}

	/**
	 * Emit some information about the OpenAL rendering device.
	 * 
	 * @return
	 */
	@Override
	String toString() {

		return """
			OpenAL audio renderer
			 - Vendor: ${alGetString(AL_VENDOR)}
			 - Device name: ${alGetString(AL_RENDERER)}
			 - OpenAL version: ${alGetString(AL_VERSION)}
		""".stripIndent()
	}

	@Override
	void unqueueBuffers(int sourceId, int... bufferIds) {

		checkForError { -> alSourceUnqueueBuffers(sourceId, bufferIds) }
	}

	@Override
	void updateListener(Vector3f position, Vector3f velocity, Orientation orientation) {

		stackPush().withCloseable { stack ->
			checkForError { -> alListenerfv(AL_POSITION, position.get(stack.mallocFloat(Vector3f.FLOATS))) }
//		checkForError { -> alListenerfv(AL_VELOCITY, velocity as float[]) }
//		checkForError { -> alListenerfv(AL_ORIENTATION, orientation as float[]) }
		}
	}

	@Override
	void updateSource(int sourceId, Vector3f position, Vector3f direction, Vector3f velocity) {

		stackPush().withCloseable { stack ->
			checkForError { -> alSourcefv(sourceId, AL_POSITION, position.get(stack.mallocFloat(Vector3f.FLOATS))) }
//		checkForError { -> alSourcefv(sourceId, AL_DIRECTION, direction as float[]) }
//		checkForError { -> alSourcefv(sourceId, AL_VELOCITY, velocity as float[]) }
		}
	}

	@Override
	void updateVolume(float volume) {

		checkForError { -> alListenerf(AL_GAIN, volume) }
	}
}
