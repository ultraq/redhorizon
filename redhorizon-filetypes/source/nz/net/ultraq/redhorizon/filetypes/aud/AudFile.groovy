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

package nz.net.ultraq.redhorizon.filetypes.aud

import nz.net.ultraq.redhorizon.codecs.IMAADPCM16bit
import nz.net.ultraq.redhorizon.codecs.WSADPCM8bit
import nz.net.ultraq.redhorizon.filetypes.FileExtensions
import nz.net.ultraq.redhorizon.filetypes.SoundFile
import nz.net.ultraq.redhorizon.filetypes.Streaming
import nz.net.ultraq.redhorizon.filetypes.Worker
import nz.net.ultraq.redhorizon.io.NativeDataInputStream

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService

/**
 * Implementation of the AUD files used in Red Alert and Tiberium Dawn.  An AUD
 * file is the sound format of choice for these games, compressed using one of 2
 * schemes: IMA-ADPCM and WS-ADPCM the latter being a Westwood proprietary
 * format.
 * <p>
 * For more information, see: http://vladan.bato.net/cnc/aud3.txt
 * 
 * @author Emanuel Rabina
 */
@FileExtensions(['aud', 'v00'])
class AudFile implements SoundFile, Streaming {

	private static final Logger logger = LoggerFactory.getLogger(AudFile)

	private static final byte TYPE_IMA_ADPCM = 99
	private static final byte TYPE_WS_ADPCM  = 1
	private static final byte FLAG_16BIT  = 0x02
	private static final byte FLAG_STEREO = 0x01

	private final NativeDataInputStream input

	// File header
	final int frequency // Stored in file as short
	final int compressedSize
	final int uncompressedSize
	final byte flags
	final byte type

	final int bitrate
	final int channels

	/**
	 * Constructor, creates a new AUD file from the data in the input stream.
	 * 
	 * @param inputStream Input stream over an AUD file.
	 */
	AudFile(InputStream inputStream) {

		input = new NativeDataInputStream(inputStream)

		// File header
		frequency        = input.readShort()
		compressedSize   = input.readInt()
		uncompressedSize = input.readInt()
		flags            = input.readByte()
		type             = input.readByte()

		bitrate = (flags & FLAG_16BIT) ? 16 : 8
		channels = (flags & FLAG_STEREO) ? 2 : 1
	}

	@Override
	ByteBuffer getSoundData(ExecutorService executorService) {

		def samples = []
		executorService
			.submit(getStreamingDataWorker { samples << it })
			.get()
		return ByteBuffer.fromBuffers(*samples)
	}

	/**
	 * Returns a worker that can be run to start streaming sound data to the
	 * {@code sampleHandler} closure.
	 * 
	 * @param sampleHandler
	 *   Closure that is called by the worker for doing something with a small
	 *   sample of sound.
	 * @return Worker for streaming sound data.
	 */
	@Override
	Worker getStreamingDataWorker(Closure sampleHandler) {

		return new Worker() {
			@Override
			void work() {

				Thread.currentThread().name = "AudFile :: Decoding"
				logger.debug('AudFile decoding started')

				def decoder =
					type == TYPE_WS_ADPCM ? new WSADPCM8bit() :
					type == TYPE_IMA_ADPCM ? new IMAADPCM16bit() :
					null

				// Decompress the aud file data by chunks
				for (def bytesRead = 0; bytesRead < compressedSize && canContinue; ) {

					// Chunk header
					def compressedSize = input.readShort()
					def uncompressedSize = input.readShort()
					assert input.readInt() == 0x0000deaf : 'AUD chunk header ID should be "0x0000deaf"'

					// Build buffers from chunk header
					def chunkSourceBuffer = ByteBuffer.allocateNative(compressedSize)
					input.readFully(chunkSourceBuffer.array())
					def chunkDataBuffer = ByteBuffer.allocateNative(uncompressedSize)

					// Decode
					decoder.decode(chunkSourceBuffer, chunkDataBuffer)

					sampleHandler(chunkDataBuffer)
					bytesRead += 8 + compressedSize
				}

				if (!stopped) {
					logger.debug('AudFile decoding complete')
				}
			}
		}
	}

	/**
	 * Return a summary of this file.
	 * 
	 * @return
	 */
	@Override
	String toString() {

		return """
			AUD file, ${frequency}hz ${bitrate}-bit ${channels == 2 ? 'Stereo' : 'Mono'}
			Encoded using ${type == TYPE_WS_ADPCM ? 'WS ADPCM' : type == TYPE_IMA_ADPCM ? 'IMA ADPCM' : '(unknown)'} algorithm
			Compressed: ${compressedSize} bytes => Uncompressed: ${uncompressedSize} bytes
		""".stripIndent().trim()
	}
}
