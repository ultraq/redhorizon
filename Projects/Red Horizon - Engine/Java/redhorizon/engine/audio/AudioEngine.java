/*
 * Copyright 2013, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package redhorizon.engine.audio;

import redhorizon.engine.SubsystemCallback;
import redhorizon.scenegraph.Scene;

import com.jogamp.openal.ALC;
import com.jogamp.openal.ALCcontext;
import com.jogamp.openal.ALCdevice;
import com.jogamp.openal.ALFactory;

/**
 * Audio engine subsystem, manages the connection to the audio hardware and
 * rendering of audio objects.
 * 
 * @author Emanuel Rabina
 */
public class AudioEngine implements Runnable {

	private final Scene scene;
	private final SubsystemCallback callback;

	/**
	 * Constructor, initializes the audio engine and attaches it to the given
	 * scene graph and event listener callback.
	 * 
	 * @param scene
	 * @param callback
	 */
	public AudioEngine(Scene scene, SubsystemCallback callback) {

		this.scene    = scene;
		this.callback = callback;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {

		Thread.currentThread().setName("Red Horizon - Audio Engine");

		ALContextManager contextmanager = null;
		try {
			// Connect to the OpenAL device
			contextmanager = new ALContextManager();
			contextmanager.makeCurrentContext();
			scene.setListener(new Listener());
			callback.subsystemInit();

			AudioRenderer renderer = new OpenALAudioRenderer(ALFactory.getAL());
			scene.render(renderer);
		}
		finally {
			// Shut down
			if (contextmanager != null) {
				contextmanager.releaseCurrentContext();
				contextmanager.destroyCurrentContext();
			}
			callback.subsystemStop();
		}
	}

	/**
	 * OpenAL context (and device) manager for the audio engine class/thread.
	 * For any OpenAL operations to take place, a context must be made current
	 * on the executing thread.  This class takes helps take care of that.
	 */
	private class ALContextManager {

		private final ALCdevice aldevice;
		private ALCcontext alcontext;

		/**
		 * Constructor, creates the device to use for the context object.
		 */
		private ALContextManager() {

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
