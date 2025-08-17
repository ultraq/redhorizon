/*
 * Copyright 2025, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.audio

import java.nio.ByteBuffer
import javax.sound.sampled.AudioFormat.Encoding
import javax.sound.sampled.AudioSystem

/**
 * A sound decoder for OGG files.
 *
 * @author Emanuel Rabina
 */
class OggDecoder implements AudioDecoder {

	@Override
	int decode(InputStream inputStream) {

		AudioSystem.getAudioInputStream(inputStream).withCloseable { oggStream ->
			AudioSystem.getAudioInputStream(Encoding.PCM_SIGNED, oggStream).withCloseable { pcmStream ->
				var format = pcmStream.format
				// NOTE: Not streaming, but only used for a test file so OK?
				trigger(new SampleDecodedEvent(format.sampleSizeInBits, format.channels, (int)format.sampleRate, ByteBuffer.wrapNative(pcmStream.readAllBytes())))
			}
		}
		return 1
	}

	@Override
	String fileExtension() {

		return 'ogg'
	}
}
