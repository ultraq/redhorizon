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
	 * {@inheritDoc}
	 */
	@Override
	int createBuffer(ByteBuffer data, int bitrate, int channels, int frequency) {

		return alGenBuffers()
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	int createSource() {

		return alGenSources()
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void deleteBuffers(int[] bufferIds) {

		alDeleteBuffers(bufferIds)
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void deleteSource(int sourceId) {

		alDeleteSources(sourceId)
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void playSource(int sourceId) {

		alSourcePlay(sourceId)
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void queueBuffer(int sourceId, int bufferId) {

		alSourceQueueBuffers(sourceId, bufferId)
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	boolean sourceExists(int sourceId) {

		return alIsSource(sourceId)
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	boolean sourcePlaying(int sourceId) {

		return alGetSourcei(sourceId, AL_PLAYING)
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void updateListener(Vector3f position, Vector3f velocity, Orientation orientation) {

		alListenerfv(AL_POSITION, position as float[])
		alListenerfv(AL_VELOCITY, velocity as float[])
		alListenerfv(AL_ORIENTATION, orientation as float[])
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void updateSource(int sourceId, Vector3f position, Vector3f direction, Vector3f velocity) {

		alSourcefv(sourceId, AL_POSITION, position as float[])
		alSourcefv(sourceId, AL_DIRECTION, direction as float[])
		alSourcefv(sourceId, AL_VELOCITY, velocity as float[])
	}
}
