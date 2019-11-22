/* 
 * Copyright 2019, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import org.lwjgl.openal.AL
import org.lwjgl.openal.ALC
import org.lwjgl.openal.ALCCapabilities
import org.lwjgl.openal.ALCapabilities
import static org.lwjgl.openal.ALC10.*
import static org.lwjgl.system.MemoryUtil.NULL

import java.nio.ByteBuffer
import java.nio.IntBuffer

/**
 * The OpenAL context, a concept used by OpenAL to control audio output.
 * 
 * @author Emanuel Rabina
 */
class OpenALContext implements Closeable {

	private final long alDevice
	private final long alContext

	/**
	 * Constructor, build a new OpenAL context with the default OpenAL device.
	 */
	OpenALContext() {

		alDevice = alcOpenDevice((ByteBuffer)null)
		ALCCapabilities alcCapabilities = ALC.createCapabilities(alDevice)
		if (!alcCapabilities.OpenALC10) {
			throw new IllegalStateException()
		}

		alContext = alcCreateContext(alDevice, (IntBuffer)null)
		makeCurrent()
		ALCapabilities alCapabilities = AL.createCapabilities(alcCapabilities)
		if (!alCapabilities.OpenAL10) {
			throw new IllegalStateException()
		}
	}

	/**
	 * Destroys the OpenAL context, releasing this context and closing the device
	 * handle.
	 */
	@Override
	void close() {

		releaseCurrentContext()
		alcDestroyContext(alContext)
		alcCloseDevice(alDevice)
	}

	/**
	 * Makes an OpenAL context current on the executing thread.
	 */
	void makeCurrent() {

		alcMakeContextCurrent(alContext)
	}

	/**
	 * Releases the OpenAL context that is current on the executing thread.
	 */
	void releaseCurrentContext() {

		alcMakeContextCurrent(NULL)
	}
}
