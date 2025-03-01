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

import nz.net.ultraq.redhorizon.events.EventTarget

import org.joml.Vector3fc

import java.nio.ByteBuffer

/**
 * The audio renderer is responsible for setting up and playing back sounds
 * through the audio device.
 *
 * @author Emanuel Rabina
 */
interface AudioRenderer extends AutoCloseable, EventTarget {

	/**
	 * Creates and fills a sound buffer with the given data.
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
	 * Set updated audio properties of a listener.
	 */
	void updateListener(float gain, Vector3fc position)

	/**
	 * Set updated audio properties of a source.
	 */
	void updateSource(Source source, Vector3fc position)
}
