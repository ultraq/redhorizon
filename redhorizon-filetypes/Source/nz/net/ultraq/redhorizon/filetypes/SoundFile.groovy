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

import java.nio.channels.ReadableByteChannel

/**
 * Interface for filetypes for sound data (eg: AUD, WAV, etc...).
 * 
 * @author Emanuel Rabina
 */
interface SoundFile extends File {

	/**
	 * Returns the bitrate of the sound sample.
	 * 
	 * @return One of 8 or 16 bits.
	 */
	SoundBitrate getBitrate()

	/**
	 * Returns the channels used by the sound.
	 * 
	 * @return One of mono or stereo.
	 */
	SoundChannels getChannels()

	/**
	 * Returns the frequency (number of samples/second) of the sound.
	 * 
	 * @return The frequency of the sound.
	 */
	int getFrequency()

	/**
	 * Returns a byte channel to the sound data in the file.
	 * 
	 * @return Byte channel containing the sound data.
	 */
	ReadableByteChannel getSoundData()
}
