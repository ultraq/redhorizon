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
import nz.net.ultraq.redhorizon.filetypes.Worker
import nz.net.ultraq.redhorizon.io.NativeDataInputStream
import static nz.net.ultraq.redhorizon.filetypes.SoundFile.Bitrate.*
import static nz.net.ultraq.redhorizon.filetypes.SoundFile.Channels.*

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
class AudFile implements SoundFile {

	private static final Logger logger = LoggerFactory.getLogger(AudFile)

	private static final byte TYPE_IMA_ADPCM = 99
	private static final byte TYPE_WS_ADPCM  = 1
	private static final byte FLAG_16BIT  = 0x02
	private static final byte FLAG_STEREO = 0x01

	private final NativeDataInputStream input

	// File header
	final int frequency
	final int compressedSize
	final int uncompressedSize
	final byte flags
	final byte type

	final Bitrate bitrate
	final Channels channels

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

		bitrate = (flags & FLAG_16BIT) ? BITRATE_16 : BITRATE_8
		channels = (flags & FLAG_STEREO) ? CHANNELS_STEREO : CHANNELS_MONO
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	Worker getSoundDataWorker() {

		return new Worker() {
			boolean complete

			@Override
			void work(ExecutorService executorService, Closure handler) {

				executorService.execute({ ->
					Thread.currentThread().name = "AudFile :: Decoding"
					logger.debug('AudFile decoding started')

					def bytesRead = 0
					def decoder =
						type == TYPE_WS_ADPCM ? new WSADPCM8bit() :
						type == TYPE_IMA_ADPCM ? new IMAADPCM16bit() :
						null
					def index = new byte[4]
					def sample = new byte[4]

					// Decompress the aud file data by chunks
					while (bytesRead < compressedSize) {

						// Chunk header
						def compressedSize   = input.readShort()
						def uncompressedSize = input.readShort()
						assert input.readInt() == 0x0000deaf : 'AUD chunk header ID should be "0x0000deaf"'

						// Build buffers from chunk header
						def chunkSourceBuffer = ByteBuffer.allocateNative(compressedSize)
						input.readFully(chunkSourceBuffer.array())
						def chunkDataBuffer = ByteBuffer.allocateNative(uncompressedSize)

						// Decode
						switch (type) {
						case TYPE_WS_ADPCM:
							decoder.decode(chunkSourceBuffer, chunkDataBuffer)
							break
						case TYPE_IMA_ADPCM:
							decoder.decode(chunkSourceBuffer, chunkDataBuffer,
								ByteBuffer.wrapNative(index), ByteBuffer.wrapNative(sample))
							break
						}

						handler(chunkDataBuffer)
						bytesRead += 8 + compressedSize
					}

					logger.debug('AudFile decoding complete')
					complete = true
				})
			}
		}
	}
}
