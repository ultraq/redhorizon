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
 * A sound decoder for file formats provided in the
 * <a href="https://github.com/hendriks73/ffsampledsp">ffsamplessp</a> project,
 * which in turn provides the decoder for use with the {@code javax.sound.samples}
 * package.
 *
 * @author Emanuel Rabina
 */
class FFSampledSPAudioDecoder implements AudioDecoder {

	final String[] supportedFileExtensions = ['mp3', 'ogg']

	@Override
	DecodeSummary decode(InputStream inputStream) {

		def (encoding, bits, channels, frequency, numSamples) = AudioSystem.getAudioInputStream(inputStream).withCloseable { encodedStream ->
			return AudioSystem.getAudioInputStream(Encoding.PCM_SIGNED, encodedStream).withCloseable { pcmStream ->
				var format = pcmStream.format
				var bits = format.sampleSizeInBits
				var channels = format.channels
				var frequency = (int)format.sampleRate
				var frameLength = pcmStream.frameLength
				var numFrames = 0

				// No frame length, read everything in one go
				if (frameLength == AudioSystem.NOT_SPECIFIED) {
					trigger(new SampleDecodedEvent(bits, channels, frequency, ByteBuffer.wrapNative(pcmStream.readAllBytes())))
					numFrames = 1
				}
				// Decode frame-by-frame
				else {
					while (numFrames < frameLength && !Thread.interrupted()) {
						var frameBytes = new byte[format.frameSize]
						pcmStream.read(frameBytes)
						trigger(new SampleDecodedEvent(bits, channels, frequency, ByteBuffer.wrapNative(frameBytes)))
						numFrames++
						Thread.yield()
					}
				}

				return new Tuple5<Encoding, Integer, Integer, Integer, Integer>(
					encodedStream.format.encoding, bits, channels, frequency, numFrames)
			}
		}

		return new DecodeSummary(bits, channels, frequency, numSamples,
			"${encoding} file, ${frequency}hz ${bits}-bit ${channels == 2 ? 'Stereo' : 'Mono'}")
	}
}
