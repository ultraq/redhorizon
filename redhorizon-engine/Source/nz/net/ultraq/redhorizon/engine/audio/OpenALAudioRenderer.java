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

package nz.net.ultraq.redhorizon.engine.audio;

/**
 * OpenAL audio renderer, plays audio on the user's computer using the OpenAL
 * API.
 * 
 * @author Emanuel Rabina
 */
public class OpenALAudioRenderer implements AudioRenderer {

	private OpenALContextManager contextmanager;
	private AL al;

	/**
	 * Constructor, creates a new OpenAL audio renderer.
	 */
	OpenALAudioRenderer() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void cleanup() {

		contextmanager.releaseCurrentContext();
		contextmanager.destroyCurrentContext();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize() {

		contextmanager = new OpenALContextManager();
		contextmanager.makeCurrentContext();
		al = ALFactory.getAL();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateListener(Listener listener) {

		al.alListenerfv(AL_POSITION, listener.getPosition().toArray(), 0);
		al.alListenerfv(AL_VELOCITY, listener.getVelocity().toArray(), 0);
		al.alListenerfv(AL_ORIENTATION, listener.getOrientation().toArray(), 0);
	}

	/**
	 * OpenAL context (and device) manager for the audio engine class/thread.
	 * For any OpenAL operations to take place, a context must be made current
	 * on the executing thread.
	 */
	private class OpenALContextManager {

		private final ALCdevice aldevice;
		private ALCcontext alcontext;

		/**
		 * Constructor, creates the device to use for the context object.
		 */
		private OpenALContextManager() {

			ALC alc = ALFactory.getALC();
			aldevice = alc.alcOpenDevice(null);
		}

		/**
		 * Destroys the OpenAL context for the currently executing thread.  Also
		 * closes the OpenAL device handle.
		 */
		private void destroyCurrentContext() {

			ALC alc = ALFactory.getALC();
			if (alcontext != null) {
				alc.alcDestroyContext(alcontext);
				alcontext = null;
			}
			alc.alcCloseDevice(aldevice);
		}

		/**
		 * Makes an OpenAL context current on the executing thread.
		 */
		private void makeCurrentContext() {

			ALC alc = ALFactory.getALC();

			// Use the current context, or make a new one
			if (alcontext == null) {
				alcontext = alc.alcCreateContext(aldevice, null);
			}
			alc.alcMakeContextCurrent(alcontext);
		}

		/**
		 * Releases the OpenAL context that is current on the executing thread.
		 */
		private void releaseCurrentContext() {

			ALC alc = ALFactory.getALC();
			if (alcontext != null) {
				alc.alcMakeContextCurrent(null);
			}
		}
	}
}
