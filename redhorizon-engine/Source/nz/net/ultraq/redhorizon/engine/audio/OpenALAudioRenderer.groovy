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

import org.lwjgl.openal.AL
import org.lwjgl.openal.ALC
import org.lwjgl.openal.ALCCapabilities
import org.lwjgl.openal.ALCapabilities
import static org.lwjgl.openal.ALC10.*

/**
 * OpenAL audio renderer, plays audio on the user's computer using the OpenAL
 * API.
 * 
 * @author Emanuel Rabina
 */
class OpenALAudioRenderer implements AudioRenderer {

	private final long alDevice
	private long alContext

	/**
	 * Constructor, creates a new OpenAL context on the current thread and makes
	 * this thread useable for rendering sounds.
	 */
	OpenALAudioRenderer() {

		def defaultDeviceName = alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER)
		alDevice = alcOpenDevice(defaultDeviceName)


		ALCCapabilities alcCapabilities = ALC.createCapabilities(alDevice)
		ALCapabilities alCapabilities = AL.createCapabilities(alcCapabilities)

		// Move to own Listener class that exists within a scene
//		al.alListenerfv(AL_POSITION, listener.getPosition().toArray(), 0)
//		al.alListenerfv(AL_VELOCITY, listener.getVelocity().toArray(), 0)
//		al.alListenerfv(AL_ORIENTATION, listener.getOrientation().toArray(), 0)
	}

	/**
	 * Destroys the OpenAL context for the currently executing thread.  Also
	 * closes the OpenAL device handle.
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
	void makeCurrentContext() {

		if (!alContext) {
			alContext = alcCreateContext(alDevice, [0] as int[])
		}
		alcMakeContextCurrent(alContext)
	}

	/**
	 * Releases the OpenAL context that is current on the executing thread.
	 */
	void releaseCurrentContext() {

		alcMakeContextCurrent(null)
	}
}
