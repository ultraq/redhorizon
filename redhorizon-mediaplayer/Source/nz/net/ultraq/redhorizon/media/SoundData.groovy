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

package nz.net.ultraq.redhorizon.media

import nz.net.ultraq.redhorizon.filetypes.SoundFile

import static org.lwjgl.openal.AL10.*

import java.nio.ByteBuffer

/**
 * Class which contains handles and details of sound data obtained from a sound
 * file (those which implement the {@link SoundFile} interface).  In obtaining
 * a fully-fledged sound to use in-game, this class is much like an intermediary
 * step between the file and the {@link SoundEffect} class.  Because playable
 * sounds (sources) are obtained from pre-existing cached sounds (buffers), this
 * class acts as those buffers.
 * 
 * @author Emanuel Rabina
 */
class SoundData {

	final int bufferId

	/**
	 * Constructor, stores the data, from these raw components of audio data,
	 * into a buffer to be used by sources (<code>Playable</code> objects) in
	 * the program.
	 * 
	 * @param al		Current OpenAL pipeline.
	 * @param data		<code>ByteBuffer</code> containing the sound data.
	 * @param bitrate	Bits per sample of the sound.
	 * @param channels	Number of sound channels.
	 * @param frequency	Frequency at which the sound is sampled.
	 */
	SoundData(ByteBuffer data, int bitrate, int channels, int frequency) {

		// Discover the format of the sound
		int format = bitrate == 8 ?
				(channels == 1 ? AL_FORMAT_MONO8  : AL_FORMAT_STEREO8) :
				(channels == 1 ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16)

		// Get a buffer ID
		int[] bufferids = new int[1]
		alGenBuffers(1, bufferids, 0)
		bufferId = bufferids[0]

		// Fill the buffer with audio data
		alBufferData(bufferId, format, data, data.limit(), frequency)
	}

	/**
	 * Deletes the buffer from memory.
	 */
	void delete() {

		alDeleteBuffers([bufferId])
	}
}
