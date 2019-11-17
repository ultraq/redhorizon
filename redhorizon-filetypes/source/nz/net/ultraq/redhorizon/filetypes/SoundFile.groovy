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

package nz.net.ultraq.redhorizon.filetypes

import groovy.transform.TupleConstructor
import java.util.concurrent.ExecutorService

/**
 * Interface for filetypes for sound data (eg: AUD, WAV, etc...).
 * 
 * @author Emanuel Rabina
 */
interface SoundFile extends File {

	/**
	 * Supported bitrates.
	 */
	@TupleConstructor
	enum Bitrate {

		BITRATE_8 (8),
		BITRATE_16(16)

		final int value
	}

	/**
	 * Supported channel types.
	 */
	@TupleConstructor
	enum Channels {

		CHANNELS_MONO  (1),
		CHANNELS_STEREO(2)

		final int value
	}

	/**
	 * Returns the bitrate of the sound sample.
	 * 
	 * @return One of 8 or 16 bits.
	 */
	Bitrate getBitrate()

	/**
	 * Returns the channels used by the sound.
	 * 
	 * @return One of mono or stereo.
	 */
	Channels getChannels()

	/**
	 * Returns the frequency (number of samples/second) of the sound.
	 * 
	 * @return The frequency of the sound.
	 */
	int getFrequency()

	/**
	 * Returns a worker that can be executed to start streaming sound data to the
	 * returned byte channel.
	 * 
	 * @param executorService
	 * @return Closure for streaming sound data.
	 */
	Worker getSoundDataWorker(ExecutorService executorService)
}
