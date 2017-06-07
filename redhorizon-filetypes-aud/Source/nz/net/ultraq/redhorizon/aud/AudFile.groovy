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

package nz.net.ultraq.redhorizon.aud

import nz.net.ultraq.redhorizon.codecs.IMAADPCM16bit
import nz.net.ultraq.redhorizon.codecs.WSADPCM8bit
import nz.net.ultraq.redhorizon.filetypes.AbstractFile
import nz.net.ultraq.redhorizon.filetypes.FileExtensions
import nz.net.ultraq.redhorizon.filetypes.SoundBitrate
import nz.net.ultraq.redhorizon.filetypes.SoundChannels
import nz.net.ultraq.redhorizon.filetypes.SoundFile
import nz.net.ultraq.redhorizon.filetypes.StreamingDataDecoder
import nz.net.ultraq.redhorizon.nio.channels.DuplicateReadOnlyByteChannel
import static nz.net.ultraq.redhorizon.filetypes.SoundBitrate.*
import static nz.net.ultraq.redhorizon.filetypes.SoundChannels.*

import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.channels.Pipe
import java.nio.channels.ReadableByteChannel
import java.nio.channels.SeekableByteChannel
import java.nio.channels.WritableByteChannel
import java.nio.file.Paths
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import static java.nio.file.StandardOpenOption.WRITE

/**
 * Implementation of the AUD files used in Red Alert and Tiberium Dawn.  An AUD
 * file is the sound format of choice for these games, compressed using one of 2
 * schemes: IMA-ADPCM and WS-ADPCM the latter being a Westwood proprietary
 * format.
 * 
 * @author Emanuel Rabina
 */
@FileExtensions("aud")
class AudFile extends AbstractFile implements SoundFile {

	private static final byte TYPE_IMA_ADPCM = 99
	private static final byte TYPE_WS_ADPCM  = 1
	private static final byte FLAG_16BIT  = 0x02
	private static final byte FLAG_STEREO = 0x01

	private final AudFileHeader header
	private final SeekableByteChannel fileData

	@Lazy
	private ExecutorService decoderThreadPool = { Executors.newCachedThreadPool() }()

	final SoundBitrate bitrate
	final SoundChannels channels
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
		input.read(headerBytes)
		headerBytes.rewind()
		header = new AudFileHeader(headerBytes)

		bitrate = (header.flags & FLAG_16BIT) != 0 ? BITRATE_16 : BITRATE_8
		channels = (header.flags & FLAG_STEREO) != 0 ? CHANNELS_STEREO : CHANNELS_MONO
		frequency = header.frequency & 0xffff

		// Store seekable channel types
		if (input instanceof SeekableByteChannel) {
			this.fileData = input
		}

		// If the input channel isn't seekable, create a temp file that is seekable
		else {
			def tempSoundFileData = File.createTempFile(name, null)
			tempSoundFileData.deleteOnExit()
			def fileChannel = FileChannel.open(Paths.get(tempSoundFileData.absolutePath), WRITE)
			fileChannel.transferFrom(input, 0, header.filesize)
			this.fileData = fileChannel
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void close() {

		fileData.close()
		decoderThreadPool.shutdownNow()
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	ReadableByteChannel getSoundData() {

		Pipe pipe = Pipe.open()
		decoderThreadPool.execute(new SoundDataDecoder(
			new DuplicateReadOnlyByteChannel(fileData), pipe.sink()))
		return pipe.source()
	}

	/**
	 * Decoder task.
	 */
	private class SoundDataDecoder extends StreamingDataDecoder {

		@Lazy
		private WSADPCM8bit decoder8Bit

		@Lazy
		private IMAADPCM16bit decoder16Bit

		/**
		 * Constructor, sets the input and output for the decoding.
		 * 
		 * @param input	Channel to read the encoded input from.
		 * @param output Channel to write the decoded output to.
		 */
		private SoundDataDecoder(ReadableByteChannel input, WritableByteChannel output) {

			super(input, output, "AudFile :: ${AudFile.this.filename} :: Sound data decoding thread")
		}

		/**
		 * Perform decoding of the sound data.
		 */
		@Override
		protected void decode() {

			ByteBuffer chunkHeaderBuffer = ByteBuffer.allocateNative(AudChunkHeader.CHUNK_HEADER_SIZE)
			int[] update = [0, 0]

			// Decompress the aud file data by chunks
			while (true) {
				chunkHeaderBuffer.clear()
				int read = input.read(chunkHeaderBuffer)
				if (read == -1) {
					break
				}
				chunkHeaderBuffer.rewind()
				ByteBuffer chunkBuffer = decodeChunk(new AudChunkHeader(chunkHeaderBuffer), update)
				output.write(chunkBuffer)
			}
		}

		/**
		 * Decodes the next chunk of audio data.  Assumes that the byte channel
		 * is positioned immediately after the chunk that is being passed-in.
		 * 
		 * @param chunkHeader Header of the chunk to decode.
		 * @param update 2-<tt>int</tt> array, containing the latest index and
		 * 				 sample values respectively.
		 * @return Decoded sound data.
		 */
		private ByteBuffer decodeChunk(AudChunkHeader chunkHeader, int[] update) {

			// Build buffers from chunk header
			def source = ByteBuffer.allocateNative(chunkHeader.filesize & 0xffff)
			input.read(source)
			source.rewind()
			def dest = ByteBuffer.allocateNative(chunkHeader.datasize & 0xffff)

			// Decode
			switch (header.type) {
			case TYPE_WS_ADPCM:
				decoder8Bit.decode(source, dest)
				break
			case TYPE_IMA_ADPCM:
				def index  = ByteBuffer.allocateNative(4).putInt(0, update[0])
				def sample = ByteBuffer.allocateNative(4).putInt(0, update[1])
				decoder16Bit.decode(source, dest, index, sample)
				update[0] = index.getInt(0)
				update[1] = sample.getInt(0)
				break
			}
			return dest
		}
	}
}
