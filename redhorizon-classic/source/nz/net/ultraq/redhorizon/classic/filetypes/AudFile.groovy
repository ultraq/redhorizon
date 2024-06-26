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

import nz.net.ultraq.redhorizon.classic.codecs.IMAADPCM16bit
import nz.net.ultraq.redhorizon.classic.codecs.WSADPCM8bit
import nz.net.ultraq.redhorizon.events.EventTarget
import nz.net.ultraq.redhorizon.filetypes.FileExtensions
import nz.net.ultraq.redhorizon.filetypes.SoundFile
import nz.net.ultraq.redhorizon.filetypes.Streaming
import nz.net.ultraq.redhorizon.filetypes.StreamingDecoder
import nz.net.ultraq.redhorizon.filetypes.StreamingSampleEvent
import nz.net.ultraq.redhorizon.filetypes.io.NativeDataInputStream

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.transform.TupleConstructor
import java.nio.ByteBuffer

/**
 * Implementation of the AUD files used in Red Alert and Tiberium Dawn.  An AUD
 * file is the sound format of choice for these games, compressed using one of 2
 * schemes: IMA-ADPCM and WS-ADPCM, the latter being a Westwood proprietary
 * format.
 * <p>
 * For more information, see:
 * <a href="http://vladan.bato.net/cnc/aud3.txt" target="_top">http://vladan.bato.net/cnc/aud3.txt</a>
 *
 * @author Emanuel Rabina
 */
@FileExtensions(['aud', 'v00'])
@SuppressWarnings('GrFinalVariableAccess')
class AudFile implements SoundFile, Streaming {

	private static final Logger logger = LoggerFactory.getLogger(AudFile)

	static final byte TYPE_IMA_ADPCM = 99
	static final byte TYPE_WS_ADPCM = 1
	static final byte FLAG_16BIT = 0x02
	static final byte FLAG_STEREO = 0x01

	private final NativeDataInputStream input

	// File header
	final int frequency // Stored in file as short
	final int compressedSize
	final int uncompressedSize
	final byte flags
	final byte type

	final int bits
	final int channels

	/**
	 * Constructor, creates a new AUD file from the data in the input stream.
	 *
	 * @param inputStream Input stream over an AUD file.
	 */
	AudFile(InputStream inputStream) {

		input = new NativeDataInputStream(inputStream)

		// File header
		frequency = input.readShort()
		assert frequency in [22050, 44100]

		compressedSize = input.readInt()
		assert compressedSize > 0

		uncompressedSize = input.readInt()
		assert 0 < compressedSize && compressedSize < uncompressedSize

		flags = input.readByte()
		assert (flags & 0x03) == flags

		type = input.readByte()
		assert type in [TYPE_IMA_ADPCM, TYPE_WS_ADPCM]

		bits = (flags & FLAG_16BIT) ? 16 : 8
		channels = (flags & FLAG_STEREO) ? 2 : 1
	}

	@Override
	ByteBuffer getSoundData() {

		var decoder = new AudFileDecoder()
		var samples = []
		decoder.on(StreamingSampleEvent) { event ->
			samples << event.sample
		}
		decoder.run()
		return ByteBuffer.fromBuffers(samples as ByteBuffer[])
	}

	/**
	 * Returns a decoder that can be run to start streaming sound data.  The
	 * decoder will emit {@link StreamingSampleEvent}s for new samples available.
	 */
	@Override
	StreamingDecoder getStreamingDecoder() {

		return new StreamingDecoder(new AudFileDecoder(true))
	}

	@Override
	boolean isForStreaming() {

		return uncompressedSize > 1048576 // 1MB
	}

	/**
	 * Return a summary of this file.
	 */
	@Override
	String toString() {

		return [
			"AUD file, ${frequency}hz ${bits}-bit ${channels == 2 ? 'Stereo' : 'Mono'}",
			"Encoded using ${type == TYPE_WS_ADPCM ? 'WS ADPCM' : 'IMA ADPCM'} algorithm",
			"Compressed: ${String.format('%,d', compressedSize)} bytes => Uncompressed: ${String.format('%,d', uncompressedSize)} bytes"
		].join(', ')
	}

	/**
	 * Decode AUD file sound data and emit as {@link StreamingSampleEvent}s.
	 */
	@TupleConstructor
	class AudFileDecoder implements Runnable, EventTarget {

		final boolean rateLimit

		@Override
		void run() {

			Thread.currentThread().name = "AudFile :: Decoding"
			logger.debug('Decoding started')

			var decoder = type == TYPE_IMA_ADPCM ? new IMAADPCM16bit() : new WSADPCM8bit()

			// Decompress the aud file data by chunks
			var headerSize = input.bytesRead
			while (input.bytesRead < headerSize + compressedSize && !Thread.interrupted()) {
				var sample = average('Decoding sample', 1f, logger) { ->

					// Chunk header
					var compressedSize = input.readShort()
					var uncompressedSize = input.readShort()
					assert input.readInt() == 0x0000deaf : 'AUD chunk header ID should be "0x0000deaf"'

					// Decode
					return decoder.decode(
						ByteBuffer.wrapNative(input.readNBytes(compressedSize)),
						ByteBuffer.allocateNative(uncompressedSize)
					)
				}
				trigger(new StreamingSampleEvent(bits, channels, frequency, sample))

				if (rateLimit) {
					Thread.sleep(20)
				}
			}

			logger.debug('Decoding complete')
		}
	}
}
