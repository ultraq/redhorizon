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

import groovy.transform.TupleConstructor
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

		var input = new AudNativeDataInputStream(new NativeDataInputStream(inputStream))

		// File header
		var frequency = input.readUnsignedShort()
		assert frequency in [22050, 44100]

		var compressedSize = input.readInt()
		assert compressedSize > 0

		var uncompressedSize = input.readInt()
		assert 0 < compressedSize && compressedSize < uncompressedSize

		var flags = input.readUnsignedByte()
		assert (flags & 0x03) == flags

		var type = input.readByte()
		assert type in [TYPE_IMA_ADPCM, TYPE_WS_ADPCM]

		var bits = (flags & FLAG_16BIT) ? 16 : 8
		var channels = (flags & FLAG_STEREO) ? 2 : 1

		trigger(new HeaderDecodedEvent(bits, channels, frequency, uncompressedSize))

		// File body
		var decoder = type == TYPE_IMA_ADPCM ? new IMAADPCM16bit() : new WSADPCM8bit()
		var framesDecoded = 0

		// Decompress the aud file data by chunks
		var headerSize = input.bytesRead
		try {
			while (input.bytesRead < headerSize + compressedSize && !Thread.currentThread().interrupted) {
				var sample = average('Decoding sample', 1f, logger) { ->
					var chunkHeader = input.readChunkHeader()
					return decoder.decode(input.readChunkData(chunkHeader), ByteBuffer.allocateNative(chunkHeader.uncompressedSize()))
				}
				trigger(new SampleDecodedEvent(bits, channels, frequency, sample))
				framesDecoded++
				Thread.yield()
			}
		}
		catch (InterruptedException ignored) {
			logger.debug('Decoding was interrupted')
		}

		return new DecodeSummary(bits, channels, frequency, framesDecoded, [
			"AUD file, ${frequency}hz ${bits}-bit ${channels == 2 ? 'Stereo' : 'Mono'}",
			"Encoded using ${type == TYPE_WS_ADPCM ? 'WS ADPCM' : 'IMA ADPCM'} algorithm",
			"Compressed: ${String.format('%,d', compressedSize)} bytes => Uncompressed: ${String.format('%,d', uncompressedSize)} bytes"
		].join(', '))
	}

	/**
	 * A wrapper around the {@link NativeDataInputStream} with convenience methods
	 * for reaching AUD file chunks
	 */
	@TupleConstructor(defaults = false)
	private class AudNativeDataInputStream {
		@Delegate
		final NativeDataInputStream input

		/**
		 * Read the chunk data following the header.
		 */
		ByteBuffer readChunkData(AudChunkHeader header) {
			return ByteBuffer.wrapNative(input.readNBytes(header.compressedSize))
		}

		/**
		 * Read the next chunk header at the current position.
		 */
		AudChunkHeader readChunkHeader() {
			return new AudChunkHeader(input.readShort(), input.readShort(), input.readInt())
		}
	}

	/**
	 * Header for each AUD chunk.
	 */
	private static record AudChunkHeader(short compressedSize, short uncompressedSize, int id) {
		public AudChunkHeader {
			assert id == 0x0000deaf : 'AUD chunk header ID should be "0x0000deaf"'
		}
	}
}
