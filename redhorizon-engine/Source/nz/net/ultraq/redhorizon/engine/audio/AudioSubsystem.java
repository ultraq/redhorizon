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

import redhorizon.engine.SubsystemCallback;
import redhorizon.scenegraph.Scene;

/**
 * Audio subsystem, manages the connection to the audio hardware and rendering
 * of audio objects.
 * 
 * @author Emanuel Rabina
 */
public class AudioSubsystem implements Runnable {

	private final Scene scene;
	private final SubsystemCallback callback;

	/**
	 * Constructor, initializes the audio engine and attaches it to the given
	 * scene graph and event listener callback.
	 * 
	 * @param scene
	 * @param callback
	 */
	public AudioSubsystem(Scene scene, SubsystemCallback callback) {

		this.scene    = scene;
		this.callback = callback;
	}

	/**
	 * Audio engine loop: builds a connection to the OpenAL device, renders
	 * audio items as necessary, cleans it all up when made to shut down.
	 */
	@Override
	public void run() {

		Thread.currentThread().setName("Game Engine - Audio Subsystem");

		AudioRenderer renderer = null;
		try {
			// Connect to the OpenAL device
			renderer = new OpenALAudioRenderer();
			renderer.initialize();
			scene.setListener(new Listener());
			callback.subsystemInit();

			// Perform the rendering loop
			scene.render(renderer);
		}
		finally {
			// Shut down
			if (renderer != null) {
				renderer.cleanup();
			}
			callback.subsystemStop();
		}
	}
}
