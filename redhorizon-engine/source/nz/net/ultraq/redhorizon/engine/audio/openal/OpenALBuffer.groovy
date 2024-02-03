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

package nz.net.ultraq.redhorizon.engine.audio.openal

import nz.net.ultraq.redhorizon.engine.audio.Buffer

import static org.lwjgl.openal.AL10.*
import static org.lwjgl.system.MemoryStack.stackPush

import java.nio.ByteBuffer

/**
 * OpenAL-specific buffer implementation.
 *
 * @author Emanuel Rabina
 */
class OpenALBuffer extends Buffer {

	final int bufferId

	/**
	 * Constructor, build and populate an OpenAL buffer from sound data.
	 *
	 * @param bits
	 * @param channels
	 * @param frequency
	 * @param data
	 */
	OpenALBuffer(int bits, int channels, int frequency, ByteBuffer data) {

		super(bits, channels, frequency)

		bufferId = alGenBuffers()

		var soundBuffer = stackPush().withCloseable { stack ->
			return stack.malloc(data.capacity())
				.put(data)
				.flip()
		}
		data.rewind()
		var format = switch (bits) {
			case 8 -> channels == 2 ? AL_FORMAT_STEREO8 : AL_FORMAT_MONO8
			case 16 -> channels == 2 ? AL_FORMAT_STEREO16 : AL_FORMAT_MONO16
			default -> 0
		}
		alBufferData(bufferId, format, soundBuffer, frequency)
	}

	@Override
	void close() {

		alDeleteBuffers(bufferId)
	}
}
