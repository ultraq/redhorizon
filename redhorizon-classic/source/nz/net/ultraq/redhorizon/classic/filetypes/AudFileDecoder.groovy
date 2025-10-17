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

package nz.net.ultraq.redhorizon.classic.filetypes

import nz.net.ultraq.redhorizon.audio.AudioDecoder
import nz.net.ultraq.redhorizon.classic.codecs.IMAADPCM16bit
import nz.net.ultraq.redhorizon.classic.codecs.WSADPCM8bit
import nz.net.ultraq.redhorizon.classic.io.NativeDataInputStream

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.ByteBuffer

/**
 * A sound decoder for the AUD files used in Red Alert and Tiberium Dawn.  An
 * AUD file is the sound format of choice for these games, compressed using one
 * of 2 schemes: IMA-ADPCM and WS-ADPCM, the latter being a Westwood proprietary
 * format.
 * <p>
 * For more information, see:
 * <a href="http://vladan.bato.net/cnc/aud3.txt" target="_top">http://vladan.bato.net/cnc/aud3.txt</a>
 *
 * @author Emanuel Rabina
 */
class AudFileDecoder implements AudioDecoder {

	private static final Logger logger = LoggerFactory.getLogger(AudFileDecoder)

	static final byte TYPE_IMA_ADPCM = 99
	static final byte TYPE_WS_ADPCM = 1
	static final byte FLAG_16BIT = 0x02
	static final byte FLAG_STEREO = 0x01

	final String[] supportedFileExtensions = ['aud', 'v00']

	@Override
	DecodeSummary decode(InputStream inputStream) {

		logger.debug('Decoding started')
		var input = new NativeDataInputStream(inputStream)

		// File header
		var frequency = input.readShort()
		assert frequency in [(short)22050, (short)44100]

		var compressedSize = input.readInt()
		assert compressedSize > 0

		var uncompressedSize = input.readInt()
		assert 0 < compressedSize && compressedSize < uncompressedSize

		var flags = input.readByte()
		assert (byte)(flags & 0x03) == flags

		var type = input.readByte()
		assert type in [TYPE_IMA_ADPCM, TYPE_WS_ADPCM]

		var bits = (flags & FLAG_16BIT) ? 16 : 8
		var channels = (flags & FLAG_STEREO) ? 2 : 1

		trigger(new HeaderDecodedEvent(bits, channels, frequency))

		// File body
		var numSamples = 0
		var decoder = type == TYPE_IMA_ADPCM ? new IMAADPCM16bit() : new WSADPCM8bit()

		// Decompress the aud file data by chunks
		var headerSize = input.bytesRead
		while (input.bytesRead < headerSize + compressedSize && !Thread.interrupted()) {
			var sample = average('Decoding sample', 1f, logger) { ->

				// Chunk header
				var chunkCompressedSize = input.readShort()
				var chunkUncompressedSize = input.readShort()
				assert input.readInt() == 0x0000deaf : 'AUD chunk header ID should be "0x0000deaf"'

				// Decode
				return decoder.decode(
					ByteBuffer.wrapNative(input.readNBytes(chunkCompressedSize)),
					ByteBuffer.allocateNative(chunkUncompressedSize)
				)
			}
			trigger(new SampleDecodedEvent(bits, channels, frequency, sample))

			numSamples++
			Thread.sleep(20)
		}

		logger.debug('Decoding complete')

		return new DecodeSummary(bits, channels, frequency, numSamples, [
			"AUD file, ${frequency}hz ${bits}-bit ${channels == 2 ? 'Stereo' : 'Mono'}",
			"Encoded using ${type == TYPE_WS_ADPCM ? 'WS ADPCM' : 'IMA ADPCM'} algorithm",
			"Compressed: ${String.format('%,d', compressedSize)} bytes => Uncompressed: ${String.format('%,d', uncompressedSize)} bytes"
		].join(', '))
	}
}
