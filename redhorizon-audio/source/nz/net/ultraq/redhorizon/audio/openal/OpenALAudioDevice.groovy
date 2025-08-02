/*
 * Copyright 2025, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import nz.net.ultraq.redhorizon.audio.AudioDevice

import org.lwjgl.openal.AL
import org.lwjgl.openal.ALC
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.openal.AL10.*
import static org.lwjgl.openal.ALC10.*
import static org.lwjgl.system.MemoryUtil.NULL

import java.nio.ByteBuffer
import java.nio.IntBuffer

/**
 * An audio device using OpenAL as the API.
 *
 * @author Emanuel Rabina
 */
class OpenALAudioDevice implements AudioDevice {

	private static final Logger logger = LoggerFactory.getLogger(OpenALAudioDevice)

	private final long device
	private final long context

	/**
	 * Create and configure a new sound device with OpenAL.
	 */
	OpenALAudioDevice() {

		device = alcOpenDevice((ByteBuffer)null)
		if (!device) {
			throw new UnsupportedOperationException('Could not open the default OpenAL device')
		}

		var alcCapabilities = ALC.createCapabilities(device)
		if (!alcCapabilities.OpenALC11) {
			throw new UnsupportedOperationException('OpenAL device does not support OpenAL 1.1')
		}

		context = alcCreateContext(device, (IntBuffer)null)
		makeCurrent()

		var alCapabilities = AL.createCapabilities(alcCapabilities)
		if (!alCapabilities.OpenAL11) {
			throw new UnsupportedOperationException('OpenAL device does not support OpenAL 1.1')
		}

		logger.debug('OpenAL device: {vendor}, {renderer}, {version}', alGetString(AL_VENDOR), alGetString(AL_RENDERER), alGetString(AL_VERSION))
	}

	@Override
	void close() {

		alcDestroyContext(context)
		alcCloseDevice(device)
	}

	@Override
	float getMasterVolume() {

		return alGetListenerf(AL_GAIN)
	}

	@Override
	void makeCurrent() {

		alcMakeContextCurrent(context)
	}

	@Override
	void releaseCurrent() {

		alcMakeContextCurrent(NULL)
	}

	@Override
	void setMasterVolume(float volume) {

		alListenerf(AL_GAIN, volume)
	}

	@Override
	<T> T withCurrent(Closure<T> closure) {

		try {
			makeCurrent()
			return closure()
		}
		finally {
			releaseCurrent()
		}
	}
}
