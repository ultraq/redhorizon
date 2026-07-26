/*
 * Copyright 2026, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import nz.net.ultraq.redhorizon.audio.api.Listener

import org.joml.Vector3fc
import static org.lwjgl.openal.AL10.*

/**
 * OpenAL-specific listener implementation.
 *
 * @author Emanuel Rabina
 */
class OpenALListener implements Listener {

	@Override
	void close() {

		// Nothing to close - listeners are implicit with each context
	}

	@Override
	Listener withGain(float gain) {

		alListenerf(AL_GAIN, gain)
		return this
	}

	@Override
	Listener withPosition(Vector3fc position) {

		alListener3f(AL_POSITION, position.x(), position.y(), position.z())
		return this
	}

	@Override
	Listener withVelocity(Vector3fc velocity) {

		alListener3f(AL_VELOCITY, velocity.x(), velocity.y(), velocity.z())
		return this
	}
}
