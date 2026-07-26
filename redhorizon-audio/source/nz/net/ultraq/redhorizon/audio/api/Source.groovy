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

package nz.net.ultraq.redhorizon.audio.api

import org.joml.Vector3fc

/**
 * An object through which to play sound data (buffers).
 *
 * @author Emanuel Rabina
 */
interface Source extends AutoCloseable {

	/**
	 * Attach a static buffer to this source for playback.
	 *
	 * @param buffer
	 * @return This object for chaining.
	 */
	Source attachBuffer(Buffer buffer)

	/**
	 * Return the number of queued buffers that have been processed for this
	 * source.
	 *
	 * @return Number of processed buffers.
	 */
	int buffersProcessed()

	/**
	 * Return whether this source has an attached static buffer.
	 */
	boolean isBufferAttached()

	/**
	 * Return whether this source is set to loop or not.
	 */
	boolean isLooping()

	/**
	 * Rether whether this source is currently paused.
	 *
	 * @return
	 */
	boolean isPaused()

	/**
	 * Return whether this source is currently playing.
	 *
	 * @return
	 */
	boolean isPlaying()

	/**
	 * Return whether this source has stopped playing.
	 *
	 * @return
	 */
	boolean isStopped()

	/**
	 * Pause playback through this source.
	 */
	Source pause()

	/**
	 * Start playing sound through this source.
	 */
	Source play()

	/**
	 * Queue buffers on to this source, making this source one that streams sound
	 * instead of having a single buffer.
	 *
	 * @param buffers
	 * @see Source#attachBuffer(Buffer)
	 */
	Source queueBuffers(Buffer... buffers)

	/**
	 * Rewind a source, resetting its state.
	 */
	Source rewind()

	/**
	 * Stop playing the sound through this source.
	 */
	Source stop()

	/**
	 * Unqueue buffers from this source.
	 */
	Source unqueueBuffers(Buffer... buffers)

	/**
	 * Set whether this source should loop the attached sound data.
	 */
	Source withLooping(boolean looping)

	/**
	 * Set the position of a source.
	 */
	Source withPosition(Vector3fc position)

	/**
	 * Set the volume of a source.
	 */
	Source withVolume(float volume)
}
