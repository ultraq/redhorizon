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

	private OpenALContextManager contextManager

	/**
	 * {@inheritDoc}
	 */
	@Override
	void close() {

		contextManager.releaseCurrentContext()
		contextManager.destroyCurrentContext()
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void initialize() {

		contextManager = new OpenALContextManager()
		contextManager.makeCurrentContext()

		ALCCapabilities alcCapabilities = ALC.createCapabilities(contextManager.aldevice)
		ALCapabilities alCapabilities = AL.createCapabilities(alcCapabilities)

		// Move to own Listener class that exists within a scene
//		al.alListenerfv(AL_POSITION, listener.getPosition().toArray(), 0)
//		al.alListenerfv(AL_VELOCITY, listener.getVelocity().toArray(), 0)
//		al.alListenerfv(AL_ORIENTATION, listener.getOrientation().toArray(), 0)
	}

	/**
	 * OpenAL context (and device) manager for the audio engine class/thread.
	 * For any OpenAL operations to take place, a context must be made current
	 * on the executing thread.
	 */
	private class OpenALContextManager {

		private final long aldevice
		private long alcontext

		/**
		 * Constructor, creates the device to use for the context object.
		 */
		private OpenALContextManager() {

			def defaultDeviceName = alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER)
			aldevice = alcOpenDevice(defaultDeviceName)
		}

		/**
		 * Destroys the OpenAL context for the currently executing thread.  Also
		 * closes the OpenAL device handle.
		 */
		private void destroyCurrentContext() {

			alcDestroyContext(alcontext)
			alcCloseDevice(aldevice)
		}

		/**
		 * Makes an OpenAL context current on the executing thread.
		 */
		private void makeCurrentContext() {

			// Use the current context, or make a new one
			if (!alcontext) {
				alcontext = alcCreateContext(aldevice, [0] as int[])
			}
			alcMakeContextCurrent(alcontext)
		}

		/**
		 * Releases the OpenAL context that is current on the executing thread.
		 */
		private void releaseCurrentContext() {

			alcMakeContextCurrent(null)
		}
	}
}
