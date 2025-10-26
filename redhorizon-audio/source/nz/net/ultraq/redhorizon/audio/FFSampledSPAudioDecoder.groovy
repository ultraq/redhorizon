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

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.ByteBuffer
import javax.sound.sampled.AudioFormat.Encoding
import javax.sound.sampled.AudioSystem

/**
 * A sound decoder for file formats provided in the
 * <a href="https://github.com/hendriks73/ffsampledsp">ffsamplessp</a> project,
 * which in turn provides the decoder for use with the {@code javax.sound.sampled}
 * package.
 *
 * @author Emanuel Rabina
 */
class FFSampledSPAudioDecoder implements AudioDecoder {

	private static final Logger logger = LoggerFactory.getLogger(FFSampledSPAudioDecoder)

	final String[] supportedFileExtensions = ['mp3', 'ogg']

	@Override
	DecodeSummary decode(InputStream inputStream) {

		logger.debug('Decoding started')

		def (encoding, bits, channels, frequency, buffers) = AudioSystem.getAudioInputStream(inputStream).withCloseable { encodedStream ->
			return AudioSystem.getAudioInputStream(Encoding.PCM_SIGNED, encodedStream).withCloseable { pcmStream ->
				var format = pcmStream.format
				var bits = format.sampleSizeInBits
				var channels = format.channels
				var frequency = (int)format.sampleRate
				var buffers = 0

				trigger(new HeaderDecodedEvent(bits, channels, frequency))

				// Create our own chunk size of 8KB
				while (!Thread.interrupted()) {
					var audioChunk = pcmStream.readNBytes(8192)
					trigger(new SampleDecodedEvent(bits, channels, frequency, ByteBuffer.wrapNative(audioChunk)))
					buffers++
					if (audioChunk.length < 8192) {
						break
					}
					Thread.yield()
				}

				logger.debug('Decoding complete')

				return new Tuple5<Encoding, Integer, Integer, Integer, Integer>(
					encodedStream.format.encoding, bits, channels, frequency, buffers)
			}
		}

		return new DecodeSummary(bits, channels, frequency, buffers,
			"${encoding} file, ${frequency}hz ${bits}-bit ${channels == 2 ? 'Stereo' : 'Mono'}")
	}
}
