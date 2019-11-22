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
import nz.net.ultraq.redhorizon.filetypes.AbstractFile
import nz.net.ultraq.redhorizon.filetypes.FileExtensions
import nz.net.ultraq.redhorizon.filetypes.SoundFile
import nz.net.ultraq.redhorizon.filetypes.Worker
import static nz.net.ultraq.redhorizon.filetypes.SoundFile.Bitrate.*
import static nz.net.ultraq.redhorizon.filetypes.SoundFile.Channels.*

import java.nio.ByteBuffer
import java.nio.channels.ReadableByteChannel
import java.util.concurrent.ExecutorService

/**
 * Implementation of the AUD files used in Red Alert and Tiberium Dawn.  An AUD
 * file is the sound format of choice for these games, compressed using one of 2
 * schemes: IMA-ADPCM and WS-ADPCM the latter being a Westwood proprietary
 * format.
 * 
 * For more information, see: http://vladan.bato.net/cnc/aud3.txt
 * 
 * @author Emanuel Rabina
 */
@FileExtensions('aud')
class AudFile extends AbstractFile implements SoundFile {

	private static final byte TYPE_IMA_ADPCM = 99
	private static final byte TYPE_WS_ADPCM  = 1
	private static final byte FLAG_16BIT  = 0x02
	private static final byte FLAG_STEREO = 0x01

	private final AudFileHeader header
	private final ReadableByteChannel input

	final Bitrate bitrate
	final Channels channels
	final int frequency

	/**
	 * Constructor, creates a new aud file with the given name and data.
	 * 
	 * @param name  The name of this file.
	 * @param input Data of this aud file.
	 */
	AudFile(String name, ReadableByteChannel input) {

		super(name)

		// AUD file header
		def headerBytes = ByteBuffer.allocateNative(AudFileHeader.HEADER_SIZE)
		input.readAndRewind(headerBytes)
		header = new AudFileHeader(headerBytes)

		bitrate = (header.flags & FLAG_16BIT) ? BITRATE_16 : BITRATE_8
		channels = (header.flags & FLAG_STEREO) ? CHANNELS_STEREO : CHANNELS_MONO
		frequency = header.frequency

		this.input = input
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void close() {

		input.close()
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	Worker getSoundDataWorker(ExecutorService executorService) {

		return new Worker() {
			boolean complete

			@Override
			void work(Closure handler) {

				executorService.execute({ ->
					Thread.currentThread().name = "AudFile :: ${filename} :: Decoding"

					def decoder8Bit = new WSADPCM8bit()
					def decoder16Bit = new IMAADPCM16bit()

					def chunkHeaderBuffer = ByteBuffer.allocateNative(AudChunkHeader.CHUNK_HEADER_SIZE)
					int[] update = [0, 0]
					def chunkCount = 0

					// Decompress the aud file data by chunks
					while (true) {
						chunkHeaderBuffer.clear()
						if (input.readAndRewind(chunkHeaderBuffer) == -1) {
							break
						}
						def chunkHeader = new AudChunkHeader(chunkHeaderBuffer)

						// Build buffers from chunk header
						def chunkSourceBuffer = ByteBuffer.allocateNative(chunkHeader.compressedSize & 0xffff)
						input.readAndRewind(chunkSourceBuffer)
						def chunkDataBuffer = ByteBuffer.allocateDirectNative(chunkHeader.uncompressedSize & 0xffff)

						// Decode
						switch (header.type) {
						case TYPE_WS_ADPCM:
							decoder8Bit.decode(chunkSourceBuffer, chunkDataBuffer)
							break
						case TYPE_IMA_ADPCM:
							def index  = ByteBuffer.allocateNative(4).putInt(0, update[0])
							def sample = ByteBuffer.allocateNative(4).putInt(0, update[1])
							decoder16Bit.decode(chunkSourceBuffer, chunkDataBuffer, index, sample)
							update[0] = index.getInt(0)
							update[1] = sample.getInt(0)
							break
						}

						handler(chunkDataBuffer)
					}

					complete = true
				})
			}
		}
	}
}
