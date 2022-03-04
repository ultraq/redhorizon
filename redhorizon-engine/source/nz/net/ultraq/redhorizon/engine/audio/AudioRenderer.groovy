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

import org.joml.Vector3f

import java.nio.ByteBuffer

/**
 * The audio renderer is responsible for setting up and playing back sounds
 * through the audio device.
 * 
 * @author Emanuel Rabina
 */
interface AudioRenderer {

	/**
	 * Attaches a single buffer to a source.
	 * 
	 * @param sourceId
	 * @param bufferId
	 */
	void attachBufferToSource(int sourceId, int bufferId)

	/**
	 * Return the number of buffers that have been processed for the given source,
	 * which is the number of buffers that can be safely unqueued from the source.
	 * 
	 * @param sourceId
	 * @return Number of processed buffers.
	 */
	int buffersProcessed(int sourceId)

	/**
	 * Creates and fills a sound buffer with the given data.
	 * 
	 * @param data
	 * @param bits
	 * @param channels
	 * @param frequency
	 * @return Buffer ID.
	 */
	int createBuffer(ByteBuffer data, int bits, int channels, int frequency)

	/**
	 * Creates a new source through which to play sound data.
	 * 
	 * @return Source ID.
	 */
	int createSource()

	/**
	 * Delete multiple buffers at once.
	 * 
	 * @param bufferIds
	 */
	void deleteBuffers(int... bufferIds)

	/**
	 * Delete a source.
	 * 
	 * @param sourceId
	 */
	void deleteSource(int sourceId)

	/**
	 * Pause playing a source.
	 * 
	 * @param sourceId
	 */
	void pauseSource(int sourceId)

	/**
	 * Start a source playing.
	 * 
	 * @param sourceId
	 */
	void playSource(int sourceId)

	/**
	 * Queue some buffers to an existing source.
	 * 
	 * @param sourceId
	 * @param bufferIds
	 */
	void queueBuffers(int sourceId, int... bufferId)

	/**
	 * Return whether a source with the given ID exists or not.
	 * 
	 * @param sourceId
	 * @return
	 */
	boolean sourceExists(int sourceId)

	/**
	 * Return whether a source is currently paused.
	 * 
	 * @param sourceId
	 * @return
	 */
	boolean sourcePaused(int sourceId)

	/**
	 * Return whether a source is currently playing.
	 * 
	 * @param sourceId
	 * @return
	 */
	boolean sourcePlaying(int sourceId)

	/**
	 * Return whether a source is currently stopped.
	 * 
	 * @param sourceId
	 * @return
	 */
	boolean sourceStopped(int sourceId)

	/**
	 * Stop playing a source.
	 * 
	 * @param sourceId
	 */
	void stopSource(int sourceId)

	/**
	 * Unqueue some buffers from an existing source.
	 * 
	 * @param sourceId
	 * @param bufferIds
	 */
	void unqueueBuffers(int sourceId, int... bufferId)

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
