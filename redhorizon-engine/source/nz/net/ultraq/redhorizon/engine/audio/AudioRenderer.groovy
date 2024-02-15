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

import nz.net.ultraq.redhorizon.engine.geometry.Orientation
import nz.net.ultraq.redhorizon.events.EventTarget

import org.joml.Vector3f

import java.nio.ByteBuffer

/**
 * The audio renderer is responsible for setting up and playing back sounds
 * through the audio device.
 *
 * @author Emanuel Rabina
 */
interface AudioRenderer extends EventTarget {

	/**
	 * Creates and fills a sound buffer with the given data.
	 *
	 * @param data
	 * @param bits
	 * @param channels
	 * @param frequency
	 * @return Buffer
	 */
	Buffer createBuffer(int bits, int channels, int frequency, ByteBuffer data)

	/**
	 * Creates a new source through which to play sound data.
	 *
	 * @return New source object.
	 */
	Source createSource()

	/**
	 * Delete an audio resource.
	 */
	void delete(AudioResource resource)

	/**
	 * Update details about the listener.
	 *
	 * @param position
	 * @param velocity
	 * @param orientation
	 */
	void updateListener(Vector3f position, Vector3f velocity, Orientation orientation)

	/**
	 * Update details about the source.
	 *
	 * @param sourceId
	 * @param position
	 * @param direction
	 * @param velocity
	 */
	void updateSource(int sourceId, Vector3f position, Vector3f direction, Vector3f velocity)

	/**
	 * Update the volume of the listener.
	 *
	 * @param volume
	 */
	void updateVolume(float volume)
}
