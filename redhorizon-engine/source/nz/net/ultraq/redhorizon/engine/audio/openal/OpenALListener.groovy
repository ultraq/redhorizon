/*
 * Copyright 2024, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.engine.audio.openal

import nz.net.ultraq.redhorizon.engine.audio.AudioRenderer
import nz.net.ultraq.redhorizon.engine.audio.Listener

import org.joml.Vector3f
import static org.lwjgl.openal.AL10.*
import static org.lwjgl.system.MemoryStack.stackPush

/**
 * An OpenAL-specific implementation of the {@link Listener}.
 *
 * @author Emanuel Rabina
 */
class OpenALListener extends Listener {

	float volume

	OpenALListener(float volume) {

		this.volume = volume
		alListenerf(AL_GAIN, volume)
	}

	@Override
	void render(AudioRenderer renderer) {

		stackPush().withCloseable { stack ->
			alListenerfv(AL_POSITION, globalPosition.get(stack.mallocFloat(Vector3f.FLOATS)))
//		checkForError { -> alListenerfv(AL_VELOCITY, velocity as float[]) }
//		checkForError { -> alListenerfv(AL_ORIENTATION, orientation as float[]) }
		}
	}
}
