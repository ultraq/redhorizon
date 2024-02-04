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

package nz.net.ultraq.redhorizon.engine.audio

/**
 * An object through which to play sound data (buffers).
 *
 * @author Emanuel Rabina
 */
abstract class Source implements AudioResource {

	/**
	 * Attach a static buffer to this source for playback.
	 *
	 * @param buffer
	 */
	abstract void attachBuffer(Buffer buffer)

	/**
	 * Rether whether this source is currently paused.
	 *
	 * @return
	 */
	abstract boolean isPaused()

	/**
	 * Return whether this source is currently playing.
	 *
	 * @return
	 */
	abstract boolean isPlaying()

	/**
	 * Return whether this source has stopped playing.
	 *
	 * @return
	 */
	abstract boolean isStopped()

	/**
	 * Pause playback through this source.
	 */
	abstract void pause()

	/**
	 * Start playing sound through this source.
	 */
	abstract void play()

	/**
	 * Queue buffers on to this source, making this source one that streams sound
	 * instead of having a single buffer.
	 *
	 * @param buffers
	 * @see Source#attachBuffer(Buffer)
	 */
	abstract void queueBuffers(Buffer... buffers)

	/**
	 * Rewind a source, resetting its state.
	 */
	abstract void rewind()

	/**
	 * Stop playing the sound through this source.
	 */
	abstract void stop()
}
