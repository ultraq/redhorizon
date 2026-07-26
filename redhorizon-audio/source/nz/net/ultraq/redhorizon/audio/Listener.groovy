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

package nz.net.ultraq.redhorizon.audio

import nz.net.ultraq.redhorizon.audio.openal.OpenALListener

import org.joml.Vector3f
import org.joml.Vector3fc

/**
 * A representation of the player's ears in the world, a listener node can be
 * used to control the mix of the audio or to attach sounds relative to the
 * player position.
 *
 * @author Emanuel Rabina
 */
class Listener extends AudioNode<Listener> {

	private final nz.net.ultraq.redhorizon.audio.api.Listener listener = new OpenALListener()

	// TODO: This should be derived from an attached movement component 🤔
	private final Vector3f velocity = new Vector3f()

	/**
	 * Return the velocity of the listener.
	 */
	Vector3fc getVelocity() {

		return velocity
	}

	@Override
	void render() {

		listener
			.withPosition(globalPosition)
			.withVelocity(velocity)
	}

	/**
	 * Set the gain on sounds coming in to the listener.
	 */
	Listener withGain(float gain) {

		listener.withGain(gain)
		return this
	}

	/**
	 * Set the listener's velocity.
	 */
	Listener withVelocity(Vector3fc velocity) {

		this.velocity.set(velocity)
		return this
	}
}
