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

import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService

/**
 * Interface for filetypes for sound data (eg: AUD, WAV, etc...).
 * 
 * @author Emanuel Rabina
 */
interface SoundFile {

	/**
	 * Returns the number of bits used for each sound sample.
	 */
	int getBitrate()

	/**
	 * Returns the number of sound channels in this file.
	 */
	int getChannels()

	/**
	 * Returns the frequency (number of samples/second) of the sound.
	 */
	int getFrequency()

	/**
	 * Returns uncompressed sound data that can be used for playback.
	 * 
	 * @param executorService
	 *   Executor that can be used for running the streaming data worker in its
	 *   own thread, if this file is of a streaming nature.  Can be {@code null}
	 *   otherwise.
	 * @return The buffer of sound data.
	 */
	ByteBuffer getSoundData(ExecutorService executorService)
}
