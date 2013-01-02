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

import com.jogamp.openal.AL;
import static com.jogamp.openal.AL.*;

/**
 * OpenAL audio renderer, plays audio on the user's computer using the OpenAL
 * API.
 * 
 * @author Emanuel Rabina
 */
public class OpenALAudioRenderer implements AudioRenderer {

	private final AL al;

	/**
	 * Constructor, sets a valid OpenAL pipeline on this renderer.
	 * 
	 * @param al
	 */
	OpenALAudioRenderer(AL al) {

		this.al = al;
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
}
